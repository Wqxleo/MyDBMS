package UI;

import Service.PreHandle;
import org.json.JSONException;

import javax.swing.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static javax.swing.UIManager.getSystemLookAndFeelClassName;

/**
 * Created by wangquanxiu at 2018/5/27 20:39
 */
public class MainFrame extends JFrame{
    private static final long serialVersionUID = 1L;
    private JFrame frame;     //主窗口
    private JTextArea input;  //输入框
    //private JButton submit;   //提交按钮
    //private JButton clear;    //清空按钮
    public static JTextArea output;  //输出框

    public MainFrame(){
        init();

    }

    public void init(){
        frame = new JFrame();
        try {
            //设置图形界面外观为当前所使用平台的外观
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //用户单击窗口关闭按钮时执行的操作
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //设置窗口矩形区域的大小
        frame.setBounds(350, 50, 700, 500);

        frame.getContentPane().setLayout(null);
        //设置窗体名称
        frame.setTitle("MyDBMS");


        //输出框
        output = new JTextArea();
        output.setBounds(5, 5, 670, 300);
        //设置为滚动视图
        JScrollPane jScrollPane = new JScrollPane(output);
        //自动滚动
        jScrollPane.setAutoscrolls(true);
        jScrollPane.setBounds(5, 5, 670, 300);
        //添加组件
        frame.add(jScrollPane);


        // 输入框
        input = new JTextArea();
        input.setBounds(5, 320, 670, 100);
        JScrollPane jsp1 = new JScrollPane(input);
        jsp1.setBounds(5, 320, 670, 100);
        frame.add(jsp1);
        frame.setVisible(true);
        //给输入框input添加监听按键事件
        input.addKeyListener(key_Listener);
        input.setText("mysql>");
    }

    KeyListener key_Listener = new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyChar() == KeyEvent.VK_ENTER){
                String sql = input.getText();
                if(!sql.equals("mysql>")){
                    //交给PreHandle处理，字符串去掉提示语句
                    try {
                        PreHandle.preHandleSql(sql.substring(6));
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    //重置输入框
                    input.setText("mysql>");
                }
            }
        }
        @Override
        public void keyReleased(KeyEvent e) {

        }


    };

}
