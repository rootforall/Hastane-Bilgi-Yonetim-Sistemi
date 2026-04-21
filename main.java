import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HastaneOtomasyonu extends JFrame implements ActionListener {

    private Connection con;
    private JTabbedPane anaSekmeler;
    
    private JTextField txtTc, txtAd, txtSoyad, txtTel, txtTakipNo, txtFaturaTutar;
    private JButton btnKaydet, btnSgkSorgula, btnMuayeneBaslat, btnReceteYaz, btnTetkikIste;
    private JButton btnProvizyon, btnFaturaKes, btnMedulaGonder, btnMalzemeDus, btnDoner;
    private JTable hastaTablosu, stokTablosu, tblAcil;
    private DefaultTableModel hastaModel, stokModel;

    private JComboBox<String> cmbPoliklinik, cmbDoktor;
    private JTextArea txtSikayet, txtTeshis;

    private boolean isRunning = true;
    private JLabel lblTarih;

    public HastaneOtomasyonu() {
        super("HBYS - Hastane Bilgi Yönetim Sistemi");
        
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        baglanVeKurulumYap();

        anaSekmeler = new JTabbedPane();
        
        anaSekmeler.addTab("Hasta Kabul & Danışma", paneliOlusturHastaKabul());
        anaSekmeler.addTab("Poliklinik & Muayene", paneliOlusturPoliklinik());
        anaSekmeler.addTab("Acil & Triyaj", paneliOlusturAcil());
        anaSekmeler.addTab("Yatan Hasta & Hemşire", paneliOlusturYatanHasta());
        anaSekmeler.addTab("Laboratuvar & Röntgen", paneliOlusturLabRadyoloji());
        anaSekmeler.addTab("Ameliyathane", paneliOlusturAmeliyathane());
        anaSekmeler.addTab("Medula & Fatura", paneliOlusturMedulaVezne());
        anaSekmeler.addTab("Eczane & Stok", paneliOlusturStok());
        anaSekmeler.addTab("Personel & Döner Sermaye", paneliOlusturPersonel());

        add(anaSekmeler, BorderLayout.CENTER);

        JPanel pnlDurum = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblTarih = new JLabel();
        pnlDurum.add(new JLabel("Durum: Aktif | Veritabanı: SQLite (hastane.db) | Kullanıcı: admin | "));
        pnlDurum.add(lblTarih);
        add(pnlDurum, BorderLayout.SOUTH);

        new Thread(() -> {
            while(isRunning) {
                try {
                    lblTarih.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()));
                    Thread.sleep(1000);
                } catch (Exception ignored) {}
            }
        }).start();

        hastaListesiniGuncelle();
        stokListesiniGuncelle();
    }

    private void baglanVeKurulumYap() {
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:hastane.db");
            Statement stmt = con.createStatement();
            
            stmt.execute("CREATE TABLE IF NOT EXISTS Hastalar (TC TEXT PRIMARY KEY, Ad TEXT, Soyad TEXT, Telefon TEXT, KayitTarihi TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Muayeneler (ID INTEGER PRIMARY KEY AUTOINCREMENT, HastaTC TEXT, Poliklinik TEXT, Doktor TEXT, Sikayet TEXT, Teshis TEXT, Tarih TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Cari (ID INTEGER PRIMARY KEY AUTOINCREMENT, HastaTC TEXT, IslemTipi TEXT, Tutar REAL, Tarih TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Stok (Malzeme TEXT PRIMARY KEY, Miktar INTEGER, Birim TEXT, Fiyat REAL)");
            
            stmt.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Veritabanı bağlantı hatası! SQLite JDBC sürücüsünün yüklü olduğundan emin olun.\nHata: " + e.getMessage());
        }
    }

    private JPanel paneliOlusturHastaKabul() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel ust = new JPanel(null);
        ust.setPreferredSize(new Dimension(1000, 200));
        ust.setBorder(BorderFactory.createTitledBorder("Yeni Hasta Kayıt / Arama"));

        JLabel l1 = new JLabel("TC Kimlik:"); l1.setBounds(20, 30, 100, 25);
        txtTc = new JTextField(); txtTc.setBounds(120, 30, 150, 25);
        btnSgkSorgula = new JButton("SGK Sorgula"); btnSgkSorgula.setBounds(280, 30, 120, 25);
        btnSgkSorgula.addActionListener(this);
        
        JLabel l2 = new JLabel("Ad:"); l2.setBounds(20, 65, 100, 25);
        txtAd = new JTextField(); txtAd.setBounds(120, 65, 150, 25);
        
        JLabel l3 = new JLabel("Soyad:"); l3.setBounds(20, 100, 100, 25);
        txtSoyad = new JTextField(); txtSoyad.setBounds(120, 100, 150, 25);

        JLabel l4 = new JLabel("Telefon:"); l4.setBounds(20, 135, 100, 25);
        txtTel = new JTextField(); txtTel.setBounds(120, 135, 150, 25);

        btnKaydet = new JButton("Sisteme Kaydet"); 
        btnKaydet.setBounds(120, 170, 200, 30);
        btnKaydet.addActionListener(this);

        ust.add(l1); ust.add(txtTc); ust.add(btnSgkSorgula);
        ust.add(l2); ust.add(txtAd);
        ust.add(l3); ust.add(txtSoyad);
        ust.add(l4); ust.add(txtTel);
        ust.add(btnKaydet);

        String[] kolonlar = {"TC", "Ad", "Soyad", "Telefon", "Kayıt Tarihi"};
        hastaModel = new DefaultTableModel(kolonlar, 0);
        hastaTablosu = new JTable(hastaModel);
        
        p.add(ust, BorderLayout.NORTH);
        p.add(new JScrollPane(hastaTablosu), BorderLayout.CENTER);

        return p;
    }

    private JPanel paneliOlusturPoliklinik() {
        JPanel p = new JPanel(new BorderLayout());
        
        JPanel sol = new JPanel(new GridLayout(6, 2, 5, 5));
        sol.setBorder(BorderFactory.createTitledBorder("Muayene Bilgileri"));
        
        sol.add(new JLabel("Poliklinik:"));
        cmbPoliklinik = new JComboBox<>(new String[]{"Dahiliye", "Göz", "KBB", "Ortopedi"});
        sol.add(cmbPoliklinik);
        
        sol.add(new JLabel("Doktor:"));
        cmbDoktor = new JComboBox<>(new String[]{"Doktor Seçiniz"});
        sol.add(cmbDoktor);

        sol.add(new JLabel("Şikayet:"));
        txtSikayet = new JTextArea(3, 20);
        sol.add(new JScrollPane(txtSikayet));

        sol.add(new JLabel("Teşhis (ICD-10):"));
        txtTeshis = new JTextArea(3, 20);
        sol.add(new JScrollPane(txtTeshis));

        btnMuayeneBaslat = new JButton("Muayeneyi Kaydet");
        btnMuayeneBaslat.addActionListener(this);
        sol.add(btnMuayeneBaslat);

        JPanel sag = new JPanel(new FlowLayout());
        sag.setBorder(BorderFactory.createTitledBorder("İşlemler / İstemler"));
        
        btnTetkikIste = new JButton("Röntgen/Lab İste");
        sag.add(btnTetkikIste);

        btnReceteYaz = new JButton("E-Reçete & E-İmza");
        sag.add(btnReceteYaz);

        p.add(sol, BorderLayout.WEST);
        p.add(sag, BorderLayout.CENTER);
        
        return p;
    }

    private JPanel paneliOlusturAcil() {
        JPanel p = new JPanel(new BorderLayout());
        String[] kolonlar = {"Hasta", "Geliş Sebebi", "Triyaj", "Doktor", "Durum"};
        DefaultTableModel acilModel = new DefaultTableModel(kolonlar, 0);
        tblAcil = new JTable(acilModel);
        tblAcil.setBackground(new Color(255, 200, 200));
        p.add(new JScrollPane(tblAcil), BorderLayout.CENTER);
        return p;
    }

    private JPanel paneliOlusturYatanHasta() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Servisler / Oda / Yatak Takip"));
        JTree treeYatak = new JTree();
        p.add(new JScrollPane(treeYatak), BorderLayout.WEST);
        JPanel hemsire = new JPanel(new GridLayout(4,1));
        hemsire.setBorder(BorderFactory.createTitledBorder("Hemşire Gözlem & İlaç"));
        hemsire.add(new JButton("Ateş/Nabız/Tansiyon Gir"));
        hemsire.add(new JButton("İlaç Uygulama (Barkod Okut)"));
        hemsire.add(new JButton("Sıvı Takip Formu"));
        hemsire.add(new JButton("Doktor Order Onayla"));
        p.add(hemsire, BorderLayout.CENTER);
        return p;
    }

    private JPanel paneliOlusturLabRadyoloji() {
        JPanel p = new JPanel(new GridLayout(1, 2));
        JPanel pLab = new JPanel(new BorderLayout());
        pLab.setBorder(BorderFactory.createTitledBorder("Laboratuvar LIS"));
        JList<String> listLab = new JList<>(new DefaultListModel<>());
        pLab.add(new JScrollPane(listLab), BorderLayout.CENTER);
        pLab.add(new JButton("Sonuçları Onayla"), BorderLayout.SOUTH);
        JPanel pRad = new JPanel(new BorderLayout());
        pRad.setBorder(BorderFactory.createTitledBorder("Radyoloji PACS/RIS"));
        JList<String> listRad = new JList<>(new DefaultListModel<>());
        pRad.add(new JScrollPane(listRad), BorderLayout.CENTER);
        pRad.add(new JButton("Rapor Yaz"), BorderLayout.SOUTH);
        p.add(pLab);
        p.add(pRad);
        return p;
    }

    private JPanel paneliOlusturAmeliyathane() {
        JPanel p = new JPanel(null);
        JTextArea txtMasa = new JTextArea("");
        txtMasa.setBounds(20, 20, 400, 150);
        p.add(txtMasa);
        JButton btnSarf = new JButton("Sarf Malzeme Düş");
        btnSarf.setBounds(20, 180, 250, 40);
        p.add(btnSarf);
        return p;
    }

    private JPanel paneliOlusturMedulaVezne() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel medula = new JPanel(new GridLayout(3, 2, 10, 10));
        medula.setBorder(BorderFactory.createTitledBorder("SGK Medula Entegrasyonu"));
        medula.add(new JLabel("Takip No:"));
        txtTakipNo = new JTextField("");
        medula.add(txtTakipNo);
        btnProvizyon = new JButton("Müstehaklık/Provizyon Al");
        medula.add(btnProvizyon);
        btnMedulaGonder = new JButton("Hizmetleri Gönder");
        medula.add(btnMedulaGonder);
        JPanel vezne = new JPanel(new GridLayout(4, 2, 10, 10));
        vezne.setBorder(BorderFactory.createTitledBorder("Vezne / Kasa / Cari"));
        vezne.add(new JLabel("Cari Seç:"));
        vezne.add(new JComboBox<>(new String[]{"Nakit Hasta", "Özel Sigorta", "SGK"}));
        vezne.add(new JLabel("Tutar:"));
        txtFaturaTutar = new JTextField("0.00 TL");
        vezne.add(txtFaturaTutar);
        btnFaturaKes = new JButton("Fatura Kes");
        vezne.add(new JLabel(""));
        vezne.add(btnFaturaKes);
        p.add(medula, BorderLayout.NORTH);
        p.add(vezne, BorderLayout.CENTER);
        return p;
    }

    private JPanel paneliOlusturStok() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Hastane Eczanesi & Ana Depo"));
        String[] cols = {"Malzeme/İlaç Adı", "Miktar", "Birim", "Fiyat"};
        stokModel = new DefaultTableModel(cols, 0);
        stokTablosu = new JTable(stokModel);
        p.add(new JScrollPane(stokTablosu), BorderLayout.CENTER);
        JPanel alt = new JPanel();
        btnMalzemeDus = new JButton("Stok Güncelle");
        alt.add(btnMalzemeDus);
        p.add(alt, BorderLayout.SOUTH);
        return p;
    }

    private JPanel paneliOlusturPersonel() {
        JPanel p = new JPanel(null);
        btnDoner = new JButton("Döner Sermaye Hesapla");
        btnDoner.setBounds(20, 20, 250, 40);
        p.add(btnDoner);
        return p;
    }

    private void hastaListesiniGuncelle() {
        if (con == null) return;
        try {
            hastaModel.setRowCount(0);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Hastalar");
            while(rs.next()) {
                hastaModel.addRow(new Object[]{
                    rs.getString("TC"), 
                    rs.getString("Ad"), 
                    rs.getString("Soyad"), 
                    rs.getString("Telefon"), 
                    rs.getString("KayitTarihi")
                });
            }
            rs.close();
            stmt.close();
        } catch (Exception ignored) {}
    }

    private void stokListesiniGuncelle() {
        if (con == null) return;
        try {
            stokModel.setRowCount(0);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Stok");
            while(rs.next()) {
                stokModel.addRow(new Object[]{
                    rs.getString("Malzeme"), 
                    rs.getInt("Miktar"), 
                    rs.getString("Birim"), 
                    rs.getDouble("Fiyat")
                });
            }
            rs.close();
            stmt.close();
        } catch (Exception ignored) {}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnKaydet) {
            String tc = txtTc.getText();
            String ad = txtAd.getText();
            String soyad = txtSoyad.getText();
            String tel = txtTel.getText();
            String tarih = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
            
            if(tc.isEmpty() || ad.isEmpty()) {
                JOptionPane.showMessageDialog(this, "TC veya Ad boş geçilemez!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (con != null) {
                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO Hastalar (TC, Ad, Soyad, Telefon, KayitTarihi) VALUES (?, ?, ?, ?, ?)");
                    ps.setString(1, tc);
                    ps.setString(2, ad);
                    ps.setString(3, soyad);
                    ps.setString(4, tel);
                    ps.setString(5, tarih);
                    ps.executeUpdate();
                    ps.close();
                    
                    PreparedStatement psCari = con.prepareStatement("INSERT INTO Cari (HastaTC, IslemTipi, Tutar, Tarih) VALUES (?, ?, ?, ?)");
                    psCari.setString(1, tc);
                    psCari.setString(2, "Kayıt Açılış");
                    psCari.setDouble(3, 0.0);
                    psCari.setString(4, tarih);
                    psCari.executeUpdate();
                    psCari.close();

                    hastaListesiniGuncelle();
                    JOptionPane.showMessageDialog(this, "Hasta veritabanına kaydedildi.");
                    
                    txtTc.setText("");
                    txtAd.setText("");
                    txtSoyad.setText("");
                    txtTel.setText("");
                } catch(Exception ex) {
                    JOptionPane.showMessageDialog(this, "Kayıt hatası: " + ex.getMessage());
                }
            }
        } 
        else if (e.getSource() == btnMuayeneBaslat) {
            if(txtSikayet.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Şikayet girmeden muayene başlatılamaz.");
            } else {
                if(hastaTablosu.getSelectedRow() == -1) {
                    JOptionPane.showMessageDialog(this, "Lütfen 'Hasta Kabul' sekmesinden bir hasta seçin.");
                    return;
                }
                String seciliTc = hastaTablosu.getValueAt(hastaTablosu.getSelectedRow(), 0).toString();
                String tarih = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date());

                if(con != null) {
                    try {
                        PreparedStatement ps = con.prepareStatement("INSERT INTO Muayeneler (HastaTC, Poliklinik, Doktor, Sikayet, Teshis, Tarih) VALUES (?, ?, ?, ?, ?, ?)");
                        ps.setString(1, seciliTc);
                        ps.setString(2, cmbPoliklinik.getSelectedItem().toString());
                        ps.setString(3, cmbDoktor.getSelectedItem().toString());
                        ps.setString(4, txtSikayet.getText());
                        ps.setString(5, txtTeshis.getText());
                        ps.setString(6, tarih);
                        ps.executeUpdate();
                        ps.close();

                        PreparedStatement psCari = con.prepareStatement("INSERT INTO Cari (HastaTC, IslemTipi, Tutar, Tarih) VALUES (?, ?, ?, ?)");
                        psCari.setString(1, seciliTc);
                        psCari.setString(2, "Muayene Ücreti");
                        psCari.setDouble(3, 150.0);
                        psCari.setString(4, tarih);
                        psCari.executeUpdate();
                        psCari.close();

                        JOptionPane.showMessageDialog(this, "Muayene veritabanına kaydedildi.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Muayene kayıt hatası: " + ex.getMessage());
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HastaneOtomasyonu frm = new HastaneOtomasyonu();
            frm.setVisible(true);
        });
    }
}
