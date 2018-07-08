import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class GUI extends Frame {
    JPanel Header, Body, Console;
    final int width = 800, height = 500;
    final int headerHeight = 40;
    final int consoleHeight = 260;
    final int bodyHeight = height - headerHeight - consoleHeight;
    Button[] headerButtons;
    final int headerButtonsCount = 5;
    final int consoleLineLimit=14;
    boolean runningState;
    public TextArea[][] areas;
    ConsoleTextArea consoleTextArea;
    MapBridging bridging;
    SolveThread solveThread;
    public GUI() {
        setWindowsExitButton();
        setResizable(false);
        setSize(width, height);
        initUserInterface();
        runningState=false;
        setVisible(true);
        consoleTextArea.addConsoleText("Ready.");
    }
    private void setWindowsExitButton() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }
    private void initUserInterface() {
        Header = new JPanel();
        Body = new JPanel();
        Console = new JPanel();

        Body.setPreferredSize(new Dimension(width, bodyHeight));
        setLayout(new BorderLayout());
        bridging=new MapBridging();
        initHeader();
        initBody();
        initConsole();
    }
    private void initBody()
    {
        Body.setLayout(new GridLayout(9,9));
        areas=new TextArea[9][9];
        for(int i=0;i<9;i++)
            for(int j=0;j<9;j++)
            {
                areas[i][j]=new TextArea("",1,1,TextArea.SCROLLBARS_NONE);
                Body.add(areas[i][j]);
            }
        bridging.setAreas(areas);
        add(Body, BorderLayout.CENTER);
    }
    private void initHeader() {
        Header.setLayout(new FlowLayout());
        Header.setPreferredSize(new Dimension(width, headerHeight));
        headerButtons = new Button[headerButtonsCount];

        ActionListener headerButtonsListener = e -> {
            switch (e.getActionCommand()) {
                case "LOAD":
                    readFile();
                    break;
                case "RUN":
                    if(runningState)
                    {
                        consoleTextArea.addConsoleText("It's running.");
                        return;
                    }
                    lockArea(false);
                    consoleTextArea.addConsoleText("Solving procedure starts.");
                    bridging.NotificationTextChanged();
                    runningState=true;
                    solveThread=new SolveThread(bridging.data.getMap(),this);
                    break;
                case "INTERRUPT":
                    if(runningState)
                        solveThread.changeUISyncState();
                    else
                        consoleTextArea.addConsoleText("Click running first.");
                    break;
                case "CLEAR":
                    bridging.Clear();
                    lockArea(true);
                    consoleTextArea.addConsoleText("Cleared");
                    break;
                case "QUIT":
                    dispose();
                    break;
            }
        };
        for (int i = 0; i < headerButtonsCount; i++) {
            headerButtons[i] = new Button();
            headerButtons[i].addActionListener(headerButtonsListener);
            Header.add(headerButtons[i]);
        }
        headerButtons[0].setLabel("LOAD");
        headerButtons[1].setLabel("RUN");
        headerButtons[2].setLabel("INTERRUPT");
        headerButtons[3].setLabel("CLEAR");
        headerButtons[4].setLabel("QUIT");
        add(Header, BorderLayout.NORTH);
    }
    private void initConsole() {
        setLayout(new FlowLayout());
        Console.setPreferredSize(new Dimension(width, consoleHeight));
        consoleTextArea = new ConsoleTextArea(consoleLineLimit);
        consoleTextArea.setBackground(Color.BLUE);
        consoleTextArea.setForeground(Color.WHITE);
        consoleTextArea.setPreferredSize(new Dimension(width - 20, consoleHeight));
        Console.add(consoleTextArea);
        add(Console, BorderLayout.SOUTH);
    }
    private SudokuMap map;

    public SudokuMap getMap() {
        return map;
    }
    public void setMap(int[][] in)
    {
        map.setMap(in);
    }
    private void readFile() {
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.showOpenDialog(this);
        File file = fileChooser.getSelectedFile();
        if (file == null) return;
        consoleTextArea.addConsoleText("Loading "+file.getName());
        try {
            FileReadHelper fileReadHelper = new FileReadHelper(file);
            map=new SudokuMap(fileReadHelper.getMap());
            bridging.setData(map);
            bridging.NotificationSudokuChanged(false);
            consoleTextArea.addConsoleText("Load successful.");
        }catch (IOException e) {
            consoleTextArea.addConsoleText("Cause an IOException!");
            return;
        }catch (ConsoleException e)
        {
            consoleTextArea.addConsoleText(e.getMessage());
        }
    }
    public boolean isInt(String str) {
        for(int i=0;i<str.length();i++)
            if(str.charAt(i)<'0'||str.charAt(i)>'9')
                return false;
        return true;
    }
    public void lockArea(boolean f)
    {
        for(int i=0;i<9;i++)
            for(int j=0;j<9;j++)
                areas[i][j].setEditable(f);
    }
    class MapBridging
    {
        private SudokuMap data;
        private TextArea[][] areas;
        boolean UIState;
        public void setData(SudokuMap in)
        {
            data=in;
        }
        public void setAreas(TextArea[][] area)
        {
            this.areas=area;
        }
        public void NotificationSudokuChanged(boolean type)
        {
            for(int i=0;i<9;i++)
                for(int j=0;j<9;j++)
                {
                    int number=data.getPix(i,j);
                    if(number==0)areas[i][j].setText("");
                    else areas[i][j].setText(""+number);
                    if(type)
                    {
                        if(number==0)
                        {
                            areas[i][j].setBackground(Color.RED);
                        }
                        else if(number<10)areas[i][j].setBackground(Color.YELLOW);
                        else areas[i][j].setBackground(Color.WHITE);
                    }
                    else if(number==0)
                        areas[i][j].setBackground(Color.WHITE);
                    else areas[i][j].setBackground(Color.YELLOW);
                }
        }
        public void NotificationTextChanged()
        {
            int[][] in=new int[9][9];
            for(int i=0;i<9;i++){
                for(int j=0;j<9;j++) {
                    String s = areas[i][j].getText();
                    if(s.length()==1&&isInt(s))
                    {
                        in[i][j]=Integer.parseInt(s);
                    }
                    else if(s.length()==0)
                    {
                        in[i][j]=0;
                    }
                    else
                    {
                        return;
                    }
                }
            }
            setData(new SudokuMap(in));
        }
        public void Clear()
        {
            for(int i=0;i<9;i++)
                for(int j=0;j<9;j++)
                {
                    areas[i][j].setText("");
                    areas[i][j].setBackground(Color.WHITE);
                }
                solveThread.uiSyncThread.stopFlag=true;
            runningState=false;
        }
    }

}
class SudokuMap implements Cloneable {
    private int[][] map;
    public SudokuMap(int[][] in) {
        map = in;
    }

    public void setMap(int[][] map) {
        for(int i=0;i<9;i++)
            for(int j=0;j<9;j++)
                this.map[i][j]=map[i][j];
    }
    public int getPix(int i,int j)
    {return map[i][j];}
    public int[][] getMap()
    {
        return map;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
class ConsoleTextArea extends TextArea {
    private int rowLimit;
    LinkedList<String> strList;

    public ConsoleTextArea(int rowLimit) {
        this.rowLimit = rowLimit;
        strList = new LinkedList<>();
        for(int i=0;i<rowLimit;i++)
            addConsoleText("");
    }

    public void addConsoleText(String s) {
        strList.offer(s);
        if (strList.size() > rowLimit)
            strList.poll();
        updateFace();
    }

    private void updateFace() {
        StringBuffer content = new StringBuffer();
        for (int i = 0; i < strList.size(); i++) {
            content.append(strList.get(i));
            if (i != strList.size() - 1)
                content.append('\n');
        }
        setText(content.toString());
    }
}

class FileReadHelper {
    private int[][] readMap;
    public FileReadHelper(File f) throws IOException, ConsoleException {
        BufferedReader br;
        br = new BufferedReader(new FileReader(f));
        String line;
        readMap=new int[9][9];
        int rowIndex=0;
        while ((line = br.readLine()) != null) {
            if(line.length()<9)
            {
                br.close();
                throw new ConsoleException("Contains fewer than 9 characters");
            }
            for(int i=0;i<9;i++)
            {
                if(line.charAt(i)==' ')continue;
                if('1'<=line.charAt(i)&&line.charAt(i)<='9')
                    readMap[rowIndex][i]=Integer.parseInt(line.substring(i,i+1));
                else
                    readMap[rowIndex][i]=0;
            }
            rowIndex++;
        }
        br.close();
        if(rowIndex<9)
            throw new ConsoleException("File contains fewer than 9 lines");
    }
    int[][] getMap()
    {
        return readMap;
    }
}
class ConsoleException extends Exception
{
    public ConsoleException(String s)
    {
        super(s);
    }
}