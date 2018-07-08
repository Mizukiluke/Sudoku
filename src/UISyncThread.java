import javax.swing.*;
import java.util.LinkedList;

public class UISyncThread extends Thread {
    public LinkedList<SudokuMap> queue;
    GUI mainUI;
    boolean isSolved;
    boolean waitFlag;
    boolean stopFlag;
    public UISyncThread(GUI mainUI)
    {
        waitFlag=false;
        stopFlag=false;
        this.mainUI=mainUI;
        queue=new LinkedList<>();
    }
    public void addChangeEvent(SudokuMap temp)
    {
        queue.offer(temp);
    }
    @Override
    synchronized public void run() {
        super.run();
        System.out.println(queue.size());
            for(int i=0;i<queue.size();i++)
            {
                SudokuMap map=queue.get(i);
                SwingUtilities.invokeLater(() -> {
                    try {
                        mainUI.bridging.setData((SudokuMap)map.clone());
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    mainUI.bridging.NotificationSudokuChanged(true);
                });
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(waitFlag)
                {
                    try
                    {
                        wait();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                if(stopFlag)
                    return;
            }

        SwingUtilities.invokeLater(() -> {
            mainUI.runningState=false;
            if(isSolved)
                mainUI.consoleTextArea.addConsoleText("Solved.");
            else
                mainUI.consoleTextArea.addConsoleText("Can't eliminate any more possibilities.");
            mainUI.lockArea(true);
        });
    }
}
