package swing; 

import java.awt.image.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.imageio.*;
 
 public class EmbedMessage extends JFrame implements ActionListener
 {
    // Tạo các nút
    JButton open = new JButton("Open"), embed = new JButton("Embed"),
        save = new JButton("Save into new file");
    // Tạo khung ghi thông tin cần nhúng
    JTextArea message = new JTextArea(10,3);
    // Ảnh gốc để nhúng
    BufferedImage sourceImage = null, embeddedImage = null;
 
 public EmbedMessage() {
    super("Nhúng thông điệp vào ảnh");
    
    // Khởi tạo giao diện
    assembleInterface();  
    this.setSize(500, 500);
    this.setLocationRelativeTo(null);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);   
    this.setVisible(true);
    this.validate();
    }
 
 private void assembleInterface() {
    JPanel p = new JPanel(new FlowLayout());
    p.add(open);
    p.add(embed);
    p.add(save);   
    this.getContentPane().add(p, BorderLayout.SOUTH);
    open.addActionListener(this);
    embed.addActionListener(this);
    save.addActionListener(this);   
 
    p = new JPanel(new GridLayout(1,1));
    p.add(new JScrollPane(message));
    message.setFont(new Font("Arial",Font.BOLD,20));
    p.setBorder(BorderFactory.createTitledBorder("Nhập thông điệp cần nhúng"));
    this.getContentPane().add(p, BorderLayout.NORTH);
    }
 
 // Hàm lắng nghe sự kiện click của các nút
 public void actionPerformed(ActionEvent ae) {
    Object o = ae.getSource();
    if(o == open)
       openImage();
    else if(o == embed)
       embedMessage();
    else if(o == save) 
       saveImage();

    }
 
 
 // Hàm mở hộp thoại để chọn ảnh đầu vào
 private java.io.File showFileDialog(final boolean open) {
    JFileChooser fc = new JFileChooser();
    javax.swing.filechooser.FileFilter ff = new javax.swing.filechooser.FileFilter() {
       public boolean accept(java.io.File f) {
          String name = f.getName().toLowerCase();
          if(open)
             return f.isDirectory() || name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".tiff") ||
                name.endsWith(".bmp") || name.endsWith(".dib");
          return f.isDirectory() || name.endsWith(".png") ||    name.endsWith(".bmp");
          }
       public String getDescription() {
          if(open)
             return "Image (*.jpg, *.jpeg, *.png, *.gif, *.tiff, *.bmp, *.dib)";
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
   
   // Trả về file ảnh đã chọn
    return f;
    }
 
 private void openImage() {
    java.io.File f = showFileDialog(true);
    try { 
        // cap nhat ảnh goc bằng ảnh đã chọn
       sourceImage = ImageIO.read(f);
    //   JLabel l = new JLabel(new ImageIcon(sourceImage));
    //   originalPane.getViewport().add(l);
    //   this.validate();
       } catch(Exception ex) { ex.printStackTrace(); }
    }
 
 private void embedMessage() {
     // mess = đoạn text nhập vào để nhúng
    String mess = message.getText();
    // tạo 1 bản sao của ảnh gốc
    embeddedImage = sourceImage.getSubimage(0,0,
       sourceImage.getWidth(),sourceImage.getHeight());
    // nhúng thông điệp vào ảnh bản sao của ảnh gốc
    embedMessage(embeddedImage, mess);

    }
 
 private void embedMessage(BufferedImage img, String mess) {
     // lấy độ dài của thông điệp
    int messageLength = mess.length();
    // lấy chiều dài, rộng, kích thước của ảnh
    int imageWidth = img.getWidth(), imageHeight = img.getHeight(),
       imageSize = imageWidth * imageHeight;
    
    // độ dài của tin nhắn (kiểu int) sẽ được lưu vào 32 bit đầu tiên, nên tin nhắn sẽ bắt đầu lưu từ bit 32 trở đi
    // Kiểm tra kích thước của ảnh có đủ để nhúng thông điệp hay không
    if(messageLength * 8 + 32 > imageSize) {
        // Nếu không đủ thì thông báo
       JOptionPane.showMessageDialog(this, "Thông điệp lớn hơn kích thước của ảnh ! Xin chọn ảnh khác lớn hơn",
          "Thông điệp quá dài", JOptionPane.ERROR_MESSAGE);
       return;
       }
    // Nếu đủ thì nhúng thông điệp
    
    // Đầu tiên nhúng độ dài của thông điệp vào 32 bit đầu của ảnh
    embedInteger(img, messageLength, 0);
 
    // Sau đó nhúng thông điệp vào ảnh
    byte b[] = mess.getBytes();     // Chuyển dữ liệu thông điệp từ String -> mảng byte
    for(int i=0; i<b.length; i++)
        // Nhúng từng byte vào ảnh (từng ký tự)
       embedByte(img, b[i], i*8+32);
    }
 
 
 // Note: MỖi pixel dùng để giấu 1 bit vào bit cuối cùng của pixel
 // Hàm nhúng độ dài thông điệp vào ảnh
 private void embedInteger(BufferedImage img, int n, int start) {
    int maxX = img.getWidth(), maxY = img.getHeight(), 
       startX = start/maxY, startY = start - startX*maxY, count=0;
    for(int i=0; i<maxX; i++) {
       for(int j=0; j<maxY; j++) {
           // lay từng pixel của ảnh
          int rgb = img.getRGB(i, j);
          // Lấy bit thứ count của độ dài thông điệp (n là độ dài của thông điệp)
          int bit = getBitValue(n, count);
          // Thay bit cuối cùng của pixel bằng giá trị bit lấy được ở trên
          rgb = setBitValue(rgb, 0, bit);
          // Cập nhật lại ảnh
          img.setRGB(i, j, rgb);
          
          // Tăng giá trị biến count, đến khi count = 32 thì dừng (độ dài thông điệp được biểu diễn 32 bit)
          count++;
          if(count == 32)
              return;
          }
       }
    }
 
 
  // Note: MỖi pixel dùng để giấu 1 bit vào bit cuối cùng của pixel
 // Hàm nhúng từng byte (ký tự) thông điệp vào ảnh
 private void embedByte(BufferedImage img, byte b, int start) {
    int maxX = img.getWidth(), maxY = img.getHeight(), 
       startX = start/maxY, startY = start - startX*maxY, count=0;
    for(int i=startX; i<maxX; i++) {
       for(int j=startY; j<maxY; j++) {
           // lay từng pixel của ảnh
          int rgb = img.getRGB(i, j);
          // lấy bit thứ count của byte b
          int bit = getBitValue(b, count);
          // Thay bit cuối cùng của pixel bằng bit lấy được ở trên 
          rgb = setBitValue(rgb, 0, bit);
          // Cập nhật lại ảnh
          img.setRGB(i, j, rgb);
          
          // Tăng giá trị count đến 8 thì dừng (mỗi byte biểu diễn bằng 8 bit)
          count++;
          if(count == 8)
              return;
          }
       }
    }
 
 private void saveImage() {
     // Thực hiện lưu lại ảnh sau khi đã nhúng thông điệp
   java.io.File f = showFileDialog(false);
    String name = f.getName();
    String ext = name.substring(name.lastIndexOf(".")+1).toLowerCase();
    if(!ext.equals("png") && !ext.equals("bmp") &&   !ext.equals("dib")) {
          ext = "png";
          f = new java.io.File(f.getAbsolutePath()+".png");
          }
    try {
       if(f.exists()) f.delete();
       ImageIO.write(embeddedImage, ext.toUpperCase(), f);
       } catch(Exception ex) { ex.printStackTrace(); }
    }


 
 private int getBitValue(int n, int location) {
     // Lay bit thứ location của n bằng cách thực hiện phép AND
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
    new EmbedMessage();
    }
}