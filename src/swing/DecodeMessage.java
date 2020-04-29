package swing;

 import java.awt.image.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.imageio.*;
 
 public class DecodeMessage extends JFrame implements ActionListener
 {
 JButton open = new JButton("Open"), decode = new JButton("Decode");
 JTextArea message = new JTextArea(10,3);
 BufferedImage image = null;
 JScrollPane imagePane = new JScrollPane();
 
 public DecodeMessage() {
    super("Lấy thông điệp được nhúng vào ảnh");
    
    // Khởi tạo giao diện
    assembleInterface();
    
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);   
    this.setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().
       getMaximumWindowBounds());
    this.setVisible(true);
    }
 
 private void assembleInterface() {
    JPanel p = new JPanel(new FlowLayout());
    p.add(open);
    p.add(decode);
    this.getContentPane().add(p, BorderLayout.NORTH);
    open.addActionListener(this);
    decode.addActionListener(this);
    
    p = new JPanel(new GridLayout(1,1));
    p.add(new JScrollPane(message));
    message.setFont(new Font("Arial",Font.BOLD,20));
    p.setBorder(BorderFactory.createTitledBorder("Thông điệp sau khi giải mã"));
    message.setEditable(false);
    this.getContentPane().add(p, BorderLayout.SOUTH);
    
    imagePane.setBorder(BorderFactory.createTitledBorder("Ảnh được nhúng thông điệp"));
   this.getContentPane().add(imagePane, BorderLayout.CENTER);
    }
 
 // Lắng nghe sự kiện click của các nút
 public void actionPerformed(ActionEvent ae) {
    Object o = ae.getSource();
    if(o == open)
       openImage();
    else if(o == decode)
       decodeMessage();
    }
 
 
 // Tạo hộp thoại để lấy ảnh
 private java.io.File showFileDialog(boolean open) {
    JFileChooser fc = new JFileChooser();
    javax.swing.filechooser.FileFilter ff = new javax.swing.filechooser.FileFilter() {
       public boolean accept(java.io.File f) {
          String name = f.getName().toLowerCase();
          return f.isDirectory() ||   name.endsWith(".png") || name.endsWith(".bmp");
          }
       public String getDescription() {
          return "Image (*.png, *.bmp)";
          }
       };
    fc.setAcceptAllFileFilterUsed(false);
    fc.addChoosableFileFilter(ff);
 
    java.io.File f = null;
    if(open && fc.showOpenDialog(this) == fc.APPROVE_OPTION)
       f = fc.getSelectedFile();
    else if(!open && fc.showSaveDialog(this) == fc.APPROVE_OPTION)
       f = fc.getSelectedFile();
    return f;
   }
 
 private void openImage() {
    java.io.File f = showFileDialog(true);
    try {   
       image = ImageIO.read(f);
       // Cập nhật ảnh đã chọn lên giao diện
       JLabel l = new JLabel(new ImageIcon(image));
      imagePane.getViewport().add(l);
       this.validate();
       } catch(Exception ex) { ex.printStackTrace(); }
    }
 
 private void decodeMessage() {
     // lấy độ dài của thông điệp
    int len = extractInteger(image, 0, 0);
    byte b[] = new byte[len];
    // lấy từng byte thông điệp (từng ký tự)
    for(int i=0; i<len; i++)
       b[i] = extractByte(image, i*8+32, 0);
    
    // hiện thị thông điệp sau khi lấy ra khỏi ảnh
    message.setText(new String(b));
    }
 
 
 // Hàm lấy độ dài thông điệp
 private int extractInteger(BufferedImage img, int start, int storageBit) {
   int maxX = img.getWidth(), maxY = img.getHeight(), count=0; 
    //   startX = start/maxY, startY = start - startX*maxY;
    int length = 0;
    for(int i=0; i<maxX && count<32; i++) {
       for(int j=0; j<maxY && count<32; j++) {
           // lấy từng pixel của ảnh
          int rgb = img.getRGB(i, j);
          // lấy bit cuối cùng của pixel
          int bit = getBitValue(rgb, storageBit);
          // cập nhật bit thứ count của chiều dài
          length = setBitValue(length, count, bit);
         
          // Tăng giá trị count đến khi bằng 32 thì ngừng (sau khi cập nhật đủ 32 bit chiều dài thông điệp)
          count++;
            
          }
       }
    // trả về dài thông điệp
    return length;
    }
 
 
 // Hàm lấy từng byte (ký tự) của thông điệp ra khỏi ảnh
 private byte extractByte(BufferedImage img, int start, int storageBit) {
    int maxX = img.getWidth(), maxY = img.getHeight(), 
       startX = start/maxY, startY = start - startX*maxY, count=0;
    byte b = 0;
    for(int i=startX; i<maxX && count<8; i++) {
       for(int j=startY; j<maxY && count<8; j++) {
           // Lấy từng pixel
          int rgb = img.getRGB(i, j);
          // lấy bit cuối cùng của pixel
          int bit = getBitValue(rgb, storageBit);
          // cập nhật bit thứ count của byte (ký tự)
          b = (byte)setBitValue(b, count, bit);
          
          // tăng count lên đến 8 thì dừng (cập nhật đủ 8 bit của 1 ký tự)
          count++;
          }
       }
    
    // trả về byte ký tự
    return b;
    }
 
 private int getBitValue(int n, int location) {
    int v = n & (int) Math.round(Math.pow(2, location));
    return v==0?0:1;
    }
 
 private int setBitValue(int n, int location, int bit) {
    int toggle = (int) Math.pow(2, location), bv = getBitValue(n, location);
    if(bv == bit)
       return n;
    if(bv == 0 && bit == 1)
       n |= toggle;
    else if(bv == 1 && bit == 0)
       n ^= toggle;
    return n;
    }
 
 public static void main(String arg[]) {
    new DecodeMessage();
    }
 }