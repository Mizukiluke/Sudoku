import java.util.LinkedList;

public class SolveThread
{
    UISyncThread uiSyncThread;
    Solve solve;
    GUI mainUI;

    public SolveThread(int[][] map, GUI mainUI)
    {
        this.mainUI = mainUI;
        mainUI.consoleTextArea.addConsoleText("Working.");
        uiSyncThread = new UISyncThread(mainUI);
        solve = new Solve(map);
        int[][] env = new int[9][9];
        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                if (map[i][j] == 0)
                {
                    env[i][j] = 0;
                    for (int k = 1; k <= 9; k++)
                    {
                        if (solve.may[i][j][k] == 0)
                        {
                            env[i][j] *= 10;
                            env[i][j] += k;
                        }
                    }
                }
                else env[i][j] = map[i][j];
            }
        }
        try {
            mainUI.bridging.setData((SudokuMap)new SudokuMap(env).clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        mainUI.bridging.NotificationSudokuChanged(true);
        mainUI.consoleTextArea.addConsoleText("Stuck.");
        uiSyncThread.isSolved = solve.run();
        System.out.println("ok");
        uiSyncThread.start();
    }
    public void changeUISyncState()
    {
        if (!uiSyncThread.waitFlag)
        {
            uiSyncThread.waitFlag=true;
            mainUI.consoleTextArea.addConsoleText("Interrupt.");
        }
        else
        {
            synchronized (uiSyncThread)
            {
                uiSyncThread.waitFlag=false;
                uiSyncThread.notify();
            }

            mainUI.consoleTextArea.addConsoleText("Resume.");
        }
    }

    class Solve
    {
        int[][] map;
        int[][][] may;
        LinkedList<Integer> q;
        int envTakeCount;
        int takeMod=2;
        final int takeSum=20;
        Solve(int[][] in)
        {
            envTakeCount=1;
            for (int i = 0; i < 9; i++)
            {
                for (int j = 0; j < 9; j++)
                    System.out.print(in[i][j] + " ");
                System.out.println();
            }
            map = new int[9][9];
            may = new int[9][9][10];
            q = new LinkedList<>();
            for (int i = 0; i < 9; i++)
                for (int j = 0; j < 9; j++)
                    for (int k = 0; k < 10; k++)
                        may[i][j][k] = 0;
            for (int i = 0; i < 9; i++)
            {
                for (int j = 0; j < 9; j++)
                {
                    map[i][j] = in[i][j];
                    if (map[i][j] == 0) q.offer(i * 9 + j);
                    else
                    {
                        antiMay(i, j, 1);
                    }
                }
            }
        }

        void antiMay(int i, int j, int v)
        {
            int num = map[i][j];
            for (int k = 0; k < 9; k++)
            {
                may[i][k][num] += v;
                may[k][j][num] += v;
            }

            for (int r = 0; r < 3; r++)
                for (int c = 0; c < 3; c++)
                    may[r + i / 3 * 3][c + j / 3 * 3][num] += v;
        }

        boolean run()
        {
            boolean f= dfs(0);
            envTakeCount=takeMod;
            makeEnv();
            int mod=uiSyncThread.queue.size()/takeSum;
            if(mod==0)return f;
            for(int i=0;i<uiSyncThread.queue.size();i++)
            {
                if(i!=uiSyncThread.queue.size()-1&&i%mod!=0)
                    uiSyncThread.queue.remove(i);
            }
            return f;
        }

        boolean dfs(int index)
        {
            makeEnv();
            if (index == q.size()) return true;
            int num = q.get(index);
            int r = num / 9;
            int c = num % 9;
            for (int i = 1; i <= 9; i++)
            {
                if (may[r][c][i] == 0)
                {
                    map[r][c] = i;
                    antiMay(r, c, 1);
                    if (dfs(index + 1)) return true;
                    antiMay(r, c, -1);
                }
            }
            //makeEnv();
            return false;
        }

        void show()
        {
            for (int i = 0; i < 9; i++)
            {
                for (int j = 0; j < 9; j++)
                    System.out.print(map[i][j] + " ");
                System.out.println();
            }
        }
        void makeEnv()
        {
            envTakeCount++;
            if(envTakeCount<takeMod)
                return;
            takeMod*=1.5;
            System.out.println(takeMod);
            int[][] env = new int[9][9];
            for (int i = 0; i < 9; i++)
            {
                for (int j = 0; j < 9; j++)
                {
                    if (map[i][j] == 0)
                    {
                        env[i][j] = 0;
                        for (int k = 1; k <= 9; k++)
                        {
                            if (may[i][j][k] == 0)
                            {
                                env[i][j] *= 10;
                                env[i][j] += k;
                            }
                        }
                    }
                    else env[i][j] = map[i][j];
                }
            }
            uiSyncThread.addChangeEvent(new SudokuMap(env));
        }
    }
}
