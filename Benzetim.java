import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;



public class Benzetim {
    private static ArrayList<String> buyrukListesi= new ArrayList<>(); //Buyruk belleğine atılacak buyrukarın listesi- program.txt HashMap e çevirilecek
    private static LinkedHashMap<String,String> buyrukBellegi= new LinkedHashMap<String,String>(); // Buyruk belleği
    private static HashMap<String,String> yazmacObegi= new HashMap<>(); //Yazmac öbeği
    private static HashMap<String,Integer> veriBelleği= new HashMap<>(); //Veri belleği
    private static HashMap<String,Integer> islemci1YurutmeZamanlari = new HashMap<String,Integer>();
    private static HashMap<String,Integer> islemci2YurutmeZamanlari = new HashMap<String,Integer>();
    private static HashMap<String,Integer> yurutulenBuyruklar = new HashMap<>();
    private static HashMap<String,String> veriBellegi = new HashMap<>(); // RAM - anlik erişim beleği
    private static String hy,ky1,ky2,vbadres,anlikDeger; // Hedef yazmacı, Kaynak yazmacı 1 , Kaynak yazmacı 2
    private static String programSayaci = "0x0000"; // Program sayacını başlangıçta 0 a ayarla
    private static File programDosyasi;
    private static File configDosyasi; // islemci 1 ' e ait config dosyası
    private static File configDosyasi2; // islemci 2 ' ye ait config dosyası
    private static int yurutulenToplamBuyruk = 0; // yürütülen toplam buyruk sayısını başlangıçta 0' a ayarla
    private static int islemciSayisi;
    private static float toplamCevrim = 0;

    public static void main(String[] args) {
        if(args.length == 2){
            islemciSayisi = 1;
            //Program ve config dosyalarını oku ve belleğe al.
            programDosyasi = new File(args[0]);
            configDosyasi = new File(args[1]);
            programDosyasiOku(programDosyasi);
            configDosyasiOku(configDosyasi,islemci1YurutmeZamanlari);
        }
        else if(args.length == 3){
            islemciSayisi = 2;
            //Program ve config dosyalarını oku ve belleğe al.
            programDosyasi = new File(args[0]);
            configDosyasi = new File(args[1]);
            configDosyasi2 = new File(args[2]);
            programDosyasiOku(programDosyasi);
            configDosyasiOku(configDosyasi,islemci1YurutmeZamanlari);
            configDosyasiOku(configDosyasi2,islemci2YurutmeZamanlari);
        }

        //Yazmac obeği ilk değerlerini 0' a ayarla
        yazmacObegiOlustur();

        //buyruk belleğini oluştur
        buyrukBelleginiOlustur();

        //veri belleğini oluştur
        veriBellegiOlustur();

        //Yazmac obeğini yazacağımız cikti.txt dosyasını oluştur
        ciktiDosyasiOlustur();

        //Gozlemlenen sonuclarin yazılacaği dosyayi olustur - sonuclar.txt
        gozlemlenenSonuclarOlustur();

        //İlk buyruğu calistir. Başlangıçta program sayacı 0x0000 a ayarlı
        buyrukCalistir(programSayaci);
    }

    //Yazmac öbeğinin başlangıç değerlerini atar
    private static void yazmacObegiOlustur(){
        for (int i = 0; i < 32; i++){
            String key = "x" + i ;
            yazmacObegi.put(key,"0");
        }
    }

    private static void veriBellegiOlustur(){
        for(int i = 0; i < 1024; i++){
            veriBellegi.put("0x" + Integer.toHexString(i),"0");
        }

    }

    private static void buyrukCalistir(String programSayaci){
        //x0' ın toprağa bağlı olduğunun benzetimi için her buyruk öncesi 0' lıyoruz
        yazmacObegi.put("x0","0");
        islemKoduAyirma(programSayaci);

    }

    private static void programDosyasiOku(File programDosyasi){
        //Program.txt yi oku ve buyrukları buyruk belleğine al
        try {
            Scanner okuyucu = new Scanner(programDosyasi);

            while(okuyucu.hasNextLine()){
                String buyruk = okuyucu.nextLine();

                if(buyruk != ""){
                    buyrukListesi.add(buyruk);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void configDosyasiOku(File configDosyasi, HashMap<String,Integer> yurutmeZamanlari){
        //will be implemented
        //config.txt yi oku ve configurasyonları belleğe al
        try {
            Scanner okuyucu = new Scanner(configDosyasi);

            while(okuyucu.hasNextLine()){
                String configLine = okuyucu.nextLine();
                String[] configurasyon = configLine.split(" ");
                yurutmeZamanlari.put(configurasyon[0], Integer.parseInt(configurasyon[1]));

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static void buyrukBelleginiOlustur(){
        for(String buyruk : buyrukListesi){
            buyrukBellegi.put(buyruk.substring(0,6), buyruk.substring(7));
        }
    }

    private static float yurutmeZamaniHesapla(HashMap<String,Integer> islemci,int islemciSayisi){


        for (HashMap.Entry<String, Integer> entry : yurutulenBuyruklar.entrySet()) {
            toplamCevrim += (float)entry.getValue() * (float)islemci.get(entry.getKey());
            //System.out.println(entry.getKey() + "/" + entry.getValue());
        }
        float yurutmeZamani = toplamCevrim / islemci.get("Frekans");

        // islemci sayisi 2 olduğunda tek tek işlemci istatistiklerini ekrana basma- ödev dosyası isteri
        if(islemciSayisi == 1){
            System.out.println("Toplam cevrim sayisi : " + toplamCevrim);
            System.out.println("Yurutulen toplam buyruk sayisi : " + yurutulenToplamBuyruk);
            System.out.println("Yurutme zamani : " + yurutmeZamani + " sn");
        }

        //Her zaman son işlmecinin değerlerini sonuclar.txt' ye yazar.
        FileWriter sonuclarYazicisi;
        try {
            sonuclarYazicisi = new FileWriter("sonuclar.txt");

            sonuclarYazicisi.write("En son calisan islemci istatistikleri" + "\n" + "Toplam cevrim sayisi : " + toplamCevrim + "\n" + "Yurutulen toplam buyruk sayisi : " + yurutulenToplamBuyruk +
                    "\n" + "Yurutme zamani : " + yurutmeZamani + " sn" + "\n");

            sonuclarYazicisi.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

        return yurutmeZamani;
    }

    private static void siradakiBuyrugaGec(String tamamlanmisBuyrukTipi) {
        //Program sayacini 4 artır
        programSayaci = programSayaciArtir(programSayaci,4);

        yurutulenToplamBuyruk++;

        //Buyruk tip ve sayılarını tutan Map' e buyruğu ekle
        try{
            yurutulenBuyruklar.put(tamamlanmisBuyrukTipi,yurutulenBuyruklar.get(tamamlanmisBuyrukTipi) + 1);
        }
        catch (NullPointerException e){
            yurutulenBuyruklar.put(tamamlanmisBuyrukTipi,1);
        }

        //yeni program sayacına karşılık gelen buyruğu çalıştır
        buyrukCalistir(programSayaci);

    }

    private static void ciktiDosyasiOlustur(){
        File ciktiDosyasi = new File("cikti.txt");
        try {
            //Eğer cikti.txt daha önce oluşturulmadi ise oluştur
            if(ciktiDosyasi.createNewFile()){
                System.out.println("Dosya olusturuldu -> " + ciktiDosyasi.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void gozlemlenenSonuclarOlustur(){
        File sonuclarDosyasi = new File("sonuclar.txt");
        try {
            //Eğer cikti.txt daha önce oluşturulmadi ise oluştur
            if(sonuclarDosyasi.createNewFile()){
                System.out.println("Dosya Olusturuldu -> " + sonuclarDosyasi.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Buyrukları al ve işlem kodlarına göre ilgili fonksiyonlara gönder
    private static void islemKoduAyirma(String programSayaci) {

        //O anda çalıştırılacak buyruğu buyruk belleğinden program sayacına göre alır
        String anlikBuyruk = buyrukBellegi.get(programSayaci);


        String[] parsedBuyruk = anlikBuyruk.split(" ");


        switch (parsedBuyruk[0]){

            case "add" : addBuyruk(parsedBuyruk); break;
            case "jal" : jalBuyruk(parsedBuyruk); break;
            case "addi" : addiBuyruk(parsedBuyruk); break;
            case "bge" : bgeBuyruk(parsedBuyruk); break;
            case "SON" : sonBuyruk(parsedBuyruk); break;
            case "jalr" : jalrBuyruk(parsedBuyruk); break;
            case "sub" : subBuyruk(parsedBuyruk); break;
            case "subi" : subiBuyruk(parsedBuyruk); break;
            case "xor" : xorBuyruk(parsedBuyruk); break;
            case "xori" : xoriBuyruk(parsedBuyruk); break;
            case "and" : andBuyruk(parsedBuyruk); break;
            case "beq" : beqBuyruk(parsedBuyruk); break;
            case "blt" : bltBuyruk(parsedBuyruk); break;
            case "lw" : lwBuyruk(parsedBuyruk); break;
            case "sw" : swBuyruk(parsedBuyruk); break;
            case "lb" : lbBuyruk(parsedBuyruk); break;
            case "sb" : sbBuyruk(parsedBuyruk); break;
            case "srl" : srlBuyruk(parsedBuyruk); break;
            case "sra" : sraBuyruk(parsedBuyruk); break;
            case "slti" : sltiBuyruk(parsedBuyruk); break;
            case "srai" : sraiBuyruk(parsedBuyruk); break;
            case "rem" : remBuyruk(parsedBuyruk); break;
            case "mul" : mulBuyruk(parsedBuyruk); break;
            default : System.exit(1);
        }
    }

    private static void mulBuyruk(String[] buyruk) {


        //Buyruğu ayır
        hy = buyruk[1];
        ky1 = buyruk[2];
        ky2 = buyruk[3];

        //Kaynak yazmaclarının değerlerini oku
        int ky1Deger = Integer.parseInt(yazmacObegi.get(ky1),16);
        int ky2Deger = Integer.parseInt(yazmacObegi.get(ky2),16);

        //Sonucu hesapla ve hedef yazmacına yaz
        int sonuc = ky1Deger * ky2Deger;

        String sonucString = "" + Integer.toHexString(sonuc);

        yazmacObegi.put(hy,sonucString);

        siradakiBuyrugaGec("R");

    }


    private static String programSayaciArtir(String anlikSayac, int artirilacakDeger){

        int decimalProgramSayaci = Integer.parseInt(anlikSayac.substring(2),16);

        String yeniSayacDegeri = Integer.toHexString(decimalProgramSayaci + artirilacakDeger);

        int basamak = yeniSayacDegeri.length();

        String geciciProgramSayaci = "0x";
        for(int i = 0; i < 4 - basamak; i++){
            geciciProgramSayaci += "0";
        }

        return geciciProgramSayaci += yeniSayacDegeri;

    }

    private static void sraiBuyruk(String[] buyruk) {

        hy = buyruk[1];
        ky1 = buyruk[2];
        anlikDeger = buyruk[3];

        int ky1deger = Integer.parseInt(yazmacObegi.get(ky1),16);
        int anlikDegerInt = Integer.parseInt(anlikDeger,16);

        int sonuc = ky1deger / anlikDegerInt;
        yazmacObegi.put(hy,Integer.toHexString(sonuc));
        siradakiBuyrugaGec("I");

    }

    private static void sltiBuyruk(String[] buyruk) {

        //slti t3, t2, 0
        hy = buyruk[1];
        ky1 = buyruk[2];
        anlikDeger = buyruk[3];

        int hy1Deger = Integer.parseInt(yazmacObegi.get(hy),16);
        int anlikDegerInt = Integer.parseInt(anlikDeger,16);

        if(hy1Deger < anlikDegerInt){
            yazmacObegi.put(hy,"1");
        }
        else{
            yazmacObegi.put(hy,"0");
        }
        siradakiBuyrugaGec("I");
    }

    private static void sraBuyruk(String[] buyruk) {

        hy = buyruk[1];
        ky1 = buyruk[2];
        ky2 = buyruk[3];

        int ky1deger = Integer.parseInt(yazmacObegi.get(ky1),16);
        int ky2deger = Integer.parseInt(yazmacObegi.get(ky2),16);

        int sonuc = ky1deger / ky2deger;
        yazmacObegi.put(hy,Integer.toHexString(sonuc));
        siradakiBuyrugaGec("R");
    }

    private static void srlBuyruk(String[] buyruk) {
        hy = buyruk[1];
        ky1 = buyruk[2];
        ky2 = buyruk[3];

        int ky1deger = Integer.parseInt(yazmacObegi.get(ky1),16);
        int ky2deger = Integer.parseInt(yazmacObegi.get(ky2),16);

        int sonuc = ky1deger / ky2deger;
        yazmacObegi.put(hy,Integer.toHexString(sonuc));
        siradakiBuyrugaGec("R");

    }

    private static void sbBuyruk(String[] buyruk) {
        //sb x5, 40(x6)

        ky1 = buyruk[1];
        anlikDeger = buyruk[2].substring(0, buyruk[2].length() - 4);
        vbadres = buyruk[2].substring(buyruk[2].length() - 3, buyruk[2].length() - 1);
        String vbAdresStr = yazmacObegi.get(vbadres);


        int ky1Deger = Integer.parseInt(yazmacObegi.get(ky1),16);
        int vbFinalAdres = Integer.parseInt(vbAdresStr,16) + Integer.parseInt(anlikDeger,16);

        veriBellegi.put("0x" + Integer.toHexString(vbFinalAdres),Integer.toHexString(ky1Deger));

        siradakiBuyrugaGec("S");

    }

    private static void lbBuyruk(String[] buyruk) {
        //lb x5, 40(x6)
        hy = buyruk[1];
        anlikDeger = buyruk[2].substring(0, buyruk[2].length() - 4);
        ky1 = buyruk[2].substring(buyruk[2].length() - 3, buyruk[2].length() - 1);

        int vbAdres = Integer.parseInt(anlikDeger,16) + Integer.parseInt(yazmacObegi.get(ky1),16);

        String vbAdresStr = "0x" + Integer.toHexString(vbAdres);
        int yazmacaYazilacakDeger = Integer.parseInt(veriBellegi.get(vbAdresStr));
        yazmacObegi.put(hy,Integer.toHexString(yazmacaYazilacakDeger));


        siradakiBuyrugaGec("I");


    }

    private static void swBuyruk(String[] buyruk) {
        //sb x5, 40(x6)

        ky1 = buyruk[1];
        anlikDeger = buyruk[2].substring(0, buyruk[2].length() - 4);
        vbadres = buyruk[2].substring(buyruk[2].length() - 3, buyruk[2].length() - 1);
        String vbAdresStr = yazmacObegi.get(vbadres);


        int ky1Deger = Integer.parseInt(yazmacObegi.get(ky1),16);
        int vbFinalAdres = Integer.parseInt(vbAdresStr,16) + Integer.parseInt(anlikDeger,16);

        veriBellegi.put("0x" + Integer.toHexString(vbFinalAdres),Integer.toHexString(ky1Deger));

        siradakiBuyrugaGec("S");
    }

    private static void lwBuyruk(String[] buyruk) {
        //lb x5, 40(x6)
        hy = buyruk[1];
        anlikDeger = buyruk[2].substring(0, buyruk[2].length() - 4);
        ky1 = buyruk[2].substring(buyruk[2].length() - 3, buyruk[2].length() - 1);

        int vbAdres = Integer.parseInt(anlikDeger,16) + Integer.parseInt(yazmacObegi.get(ky1),16);

        String vbAdresStr = "0x" + Integer.toHexString(vbAdres);
        int yazmacaYazilacakDeger = Integer.parseInt(veriBellegi.get(vbAdresStr));
        yazmacObegi.put(hy,Integer.toHexString(yazmacaYazilacakDeger));


        siradakiBuyrugaGec("I");
    }

    private static void bltBuyruk(String[] buyruk) {

        ky1 = yazmacObegi.get(buyruk[1]);
        ky2 = yazmacObegi.get(buyruk[2]);

        int ky1Deger = Integer.parseInt(ky1,16);
        int ky2Deger = Integer.parseInt(ky2,16);


        if(ky1Deger < ky2Deger){
            int atlanacakDeger = Integer.parseInt(buyruk[3],16)  * 2;
            //jal buyruğunda verilen hex değer kadar program sayacına ekle
            programSayaci = programSayaciArtir(programSayaci, atlanacakDeger);

            yurutulenToplamBuyruk++;
            try{
                yurutulenBuyruklar.put("B",yurutulenBuyruklar.get("B") + 1);
            }
            catch (NullPointerException e){
                yurutulenBuyruklar.put("B",1);
            }
            buyrukCalistir(programSayaci);
        }
        else{
            siradakiBuyrugaGec("B");
        }
    }

    private static void beqBuyruk(String[] buyruk) {

        ky1 = yazmacObegi.get(buyruk[1]);
        ky2 = yazmacObegi.get(buyruk[2]);


        int ky1Deger = Integer.parseInt(ky1,16);
        int ky2Deger = Integer.parseInt(ky2,16);

        if(ky1Deger == ky2Deger){
            int atlanacakDeger = Integer.parseInt(buyruk[3],16)  * 2;
            //jal buyruğunda verilen hex değer kadar program sayacına ekle
            programSayaci = programSayaciArtir(programSayaci, atlanacakDeger);

            yurutulenToplamBuyruk++;
            try{
                yurutulenBuyruklar.put("B",yurutulenBuyruklar.get("B") + 1);
            }
            catch (NullPointerException e){
                yurutulenBuyruklar.put("B",1);
            }
            buyrukCalistir(programSayaci);
        }
        else{
            siradakiBuyrugaGec("B");
        }
    }

    private static void andBuyruk(String[] buyruk) {

        hy = buyruk[1];
        ky1 = buyruk[2];
        ky2 = buyruk[3];
        int ky1Int = Integer.parseInt(yazmacObegi.get(ky1),16);
        String ky1Binary = Integer.toBinaryString(ky1Int);
        char[] ky1Chars = ky1Binary.toCharArray();

        int ky2Int = Integer.parseInt(yazmacObegi.get(ky2),16);
        String ky2Binary = Integer.toBinaryString(ky2Int);
        char[] ky2Chars = ky2Binary.toCharArray();

        String finalBinaryStr = "";
        // using simple for loop
        for(int i = 0; i < ky1Chars.length; i++){
            String charStr1 = "0";
            String charStr2 = "0";
            try{
                charStr1 = "" + ky1Chars[i];

            }
            catch (Exception e){
            }
            try{
                charStr2 = "" + ky2Chars[i];
            }
            catch (Exception e){

            }

            if(charStr1 == "1" && charStr2 == "1"){
                finalBinaryStr += "1";
            }
            else{
                finalBinaryStr += "0";
            }

        }

        int yazilacaDegerInt = Integer.parseInt(finalBinaryStr,2);
        yazmacObegi.put(hy,Integer.toHexString(yazilacaDegerInt));
        siradakiBuyrugaGec("R");

    }

    private static void xoriBuyruk(String[] buyruk) {

        hy = buyruk[1];
        ky1 = buyruk[2];
        anlikDeger = buyruk[3];
        int ky1Int = Integer.parseInt(yazmacObegi.get(ky1),16);
        String ky1Binary = Integer.toBinaryString(ky1Int);
        char[] ky1Chars = ky1Binary.toCharArray();

        int anlikDegerInt = Integer.parseInt(anlikDeger,16);
        String anlikDegerBinary = Integer.toBinaryString(anlikDegerInt);
        char[] anlikDegerChars = anlikDegerBinary.toCharArray();

        String finalBinaryStr = "";
        // using simple for loop
        for(int i = 0; i < ky1Chars.length; i++){
            String charStr1 = "0";
            String anlikDegerCharStr = "0";
            try{
                charStr1 = "" + ky1Chars[i];

            }
            catch (Exception e){
            }
            try{
                anlikDegerCharStr = "" + anlikDegerChars[i];
            }
            catch (Exception e){

            }
            if(charStr1 == "1" || anlikDegerCharStr == "1"){
                finalBinaryStr += "1";
            }
            else{
                finalBinaryStr += "0";
            }
        }
        int yazilacaDegerInt = Integer.parseInt(finalBinaryStr,2);
        yazmacObegi.put(hy,Integer.toHexString(yazilacaDegerInt));
        siradakiBuyrugaGec("I");
    }

    private static void xorBuyruk(String[] buyruk) {

        hy = buyruk[1];
        ky1 = buyruk[2];
        ky2 = buyruk[3];
        int ky1Int = Integer.parseInt(yazmacObegi.get(ky1),16);
        String ky1Binary = Integer.toBinaryString(ky1Int);
        char[] ky1Chars = ky1Binary.toCharArray();

        int ky2Int = Integer.parseInt(yazmacObegi.get(ky2),16);
        String ky2Binary = Integer.toBinaryString(ky2Int);
        char[] ky2Chars = ky2Binary.toCharArray();

        String finalBinaryStr = "";
        // using simple for loop
        for(int i = 0; i < ky1Chars.length; i++){
            String charStr1 = "0";
            String charStr2 = "0";
            try{
                charStr1 = "" + ky1Chars[i];

            }
            catch (Exception e){
            }
            try{
                charStr2 = "" + ky2Chars[i];
            }
            catch (Exception e){

            }

            if(charStr1 == "1" || charStr2 == "1"){
                finalBinaryStr += "1";
            }
            else{
                finalBinaryStr += "0";
            }

        }

        int yazilacaDegerInt = Integer.parseInt(finalBinaryStr,2);

        siradakiBuyrugaGec("R");
    }

    private static void addBuyruk(String[] buyruk){
        //Buyruğu ayır
        hy = buyruk[1];
        ky1 = buyruk[2];
        ky2 = buyruk[3];

        //Kaynak yazmaclarının değerlerini oku
        int ky1Deger = Integer.parseInt(yazmacObegi.get(ky1),16);
        int ky2Deger = Integer.parseInt(yazmacObegi.get(ky2),16);

        //Sonucu hesapla ve hedef yazmacına yaz
        int sonuc = ky1Deger + ky2Deger;
        String sonucString = "" + Integer.toHexString(sonuc);
        yazmacObegi.put(hy,sonucString);

        siradakiBuyrugaGec("R");

    }

    private static void addiBuyruk(String[] buyruk){

        //Buyruğu ayır
        hy = buyruk[1];
        ky1 = buyruk[2];
        anlikDeger = buyruk[3];

        //Kaynak yazmaclarının değerlerini oku
        int ky1Deger = Integer.parseInt(yazmacObegi.get(ky1),16);
        int anlikDegerInt = Integer.parseInt(anlikDeger,16);

        //Sonucu hesapla ve hedef yazmacına yaz
        int sonuc = ky1Deger + anlikDegerInt;
        String sonucString = "" + Integer.toHexString(sonuc);
        yazmacObegi.put(hy,sonucString);

        siradakiBuyrugaGec("I");
    }

    private static void subBuyruk(String[] buyruk) {

        //Buyruğu ayır
        hy = buyruk[1];
        ky1 = buyruk[2];
        ky2 = buyruk[3];

        //Kaynak yazmaclarının değerlerini oku
        int ky1Deger = Integer.parseInt(yazmacObegi.get(ky1),16);
        int ky2Deger = Integer.parseInt(yazmacObegi.get(ky2),16);

        //Sonucu hesapla ve hedef yazmacına yaz
        int sonuc = ky1Deger - ky2Deger;
        String sonucString = "" + Integer.toHexString(sonuc);
        yazmacObegi.put(hy,sonucString);

        siradakiBuyrugaGec("R");
    }

    private static void subiBuyruk(String[] buyruk) {

        //Buyruğu ayır
        hy = buyruk[1];
        ky1 = buyruk[2];
        anlikDeger = buyruk[3];

        //Kaynak yazmaclarının değerlerini oku
        int ky1Deger = Integer.parseInt(yazmacObegi.get(ky1),16);
        int anlikDegerInt = Integer.parseInt(anlikDeger,16);

        //Sonucu hesapla ve hedef yazmacına yaz
        int sonuc = ky1Deger - anlikDegerInt;
        String sonucString = "" + Integer.toHexString(sonuc);
        yazmacObegi.put(hy,sonucString);

        siradakiBuyrugaGec("I");

    }

    private static void jalBuyruk(String[] buyruk){

        hy = buyruk[1];

        String kaydedilecekProgramSayaciDegeri = programSayaciArtir(programSayaci,4).substring(2);

        //program sayacını 4 artır ve hedef yazmacına kaydet
        yazmacObegi.put(hy,kaydedilecekProgramSayaciDegeri);

        int atlanacakDeger = Integer.parseInt(buyruk[2],16) * 2 ;

        //jal buyruğunda verilen hex değer kadar program sayacına ekle
        programSayaci = programSayaciArtir(programSayaci, atlanacakDeger);

        yurutulenToplamBuyruk++;
        try{
            yurutulenBuyruklar.put("J",yurutulenBuyruklar.get("J") + 1);
        }
        catch (NullPointerException e){
            yurutulenBuyruklar.put("J",1);
        }

        buyrukCalistir(programSayaci);

    }

    private static void bgeBuyruk(String[] buyruk){

        //bge x0, rs, offset
        //0x001c bge x10 x6 6
        ky1 = yazmacObegi.get(buyruk[1]);
        ky2 = yazmacObegi.get(buyruk[2]);

        int ky1Deger = Integer.parseInt(ky1,16);
        int ky2Deger = Integer.parseInt(ky2,16);

        if(ky1Deger >= ky2Deger){
            int atlanacakDeger = Integer.parseInt(buyruk[3],16) * 2 ;
            //jal buyruğunda verilen hex değer kadar program sayacına ekle
            programSayaci = programSayaciArtir(programSayaci, atlanacakDeger);

            yurutulenToplamBuyruk++;
            try{
                yurutulenBuyruklar.put("B",yurutulenBuyruklar.get("B") + 1);
            }
            catch (NullPointerException e){
                yurutulenBuyruklar.put("B",1);
            }
            buyrukCalistir(programSayaci);
        }
        else{
            siradakiBuyrugaGec("B");
        }



    }

    private static void jalrBuyruk(String[] buyruk){ // I type

        hy = buyruk[1];
        ky1 = buyruk[2];
        anlikDeger = buyruk[3];

        ky1 = yazmacObegi.get(ky1);

        int ky1int = Integer.parseInt(ky1,16);
        int atlanacakBuyruk = ky1int +  Integer.parseInt(anlikDeger,16);

        String kaydedilecekProgramSayaciDegeri = programSayaciArtir(programSayaci,4).substring(2);
        yazmacObegi.put(hy,kaydedilecekProgramSayaciDegeri);
        programSayaci = programSayaciArtir("0x0000",atlanacakBuyruk);

        yurutulenToplamBuyruk++;
        //Buyruk tip ve sayılarını tutan Map' e buyruğu ekle
        try{
            yurutulenBuyruklar.put("J",yurutulenBuyruklar.get("J") + 1);
        }
        catch (NullPointerException e){
            yurutulenBuyruklar.put("J",1);
        }
        buyrukCalistir(programSayaci);
    }

    private static void remBuyruk(String[] buyruk) {

        //Buyruğu ayır
        hy = buyruk[1];
        ky1 = buyruk[2];
        ky2 = buyruk[3];

        //Kaynak yazmaclarının değerlerini oku
        int ky1Deger = Integer.parseInt(yazmacObegi.get(ky1),16);
        int ky2Deger = Integer.parseInt(yazmacObegi.get(ky2),16);

        //Sonucu hesapla ve hedef yazmacına yaz
        int sonuc = ky1Deger % ky2Deger;
        String sonucString = "" + Integer.toHexString(sonuc);
        yazmacObegi.put(hy,sonucString);

        siradakiBuyrugaGec("R");
    }

    private static void sonBuyruk(String[] buyruk){

        yurutulenToplamBuyruk++;

        if(islemciSayisi == 1){
            yurutmeZamaniHesapla(islemci1YurutmeZamanlari,1);
        }
        else if(islemciSayisi == 2){
            //İki işlemcinin de yürütme zamanlarını hesapla
            float islemci1Yurutme = yurutmeZamaniHesapla(islemci1YurutmeZamanlari,2);
            float islemci2Yurutme = yurutmeZamaniHesapla(islemci2YurutmeZamanlari,2);

            //Ekrana gerekli bilgileri yazdır
            if(islemci1Yurutme < islemci2Yurutme){
                System.out.println("Islemci1' in basarimi Islemci2' nin basarimindan " + (islemci2Yurutme/islemci1Yurutme) + " kat daha yuksek.");
            }
            else {
                System.out.println("Islemci2' in basarimi Islemci1' nin basarimindan " + (islemci1Yurutme/islemci2Yurutme) + " kat daha yuksek.");
            }
        }

        yazmacObegiYazdir();

        //Programı sonlandır
        System.exit(0);
    }

    private static void yazmacObegiYazdir() {
        FileWriter dosyaYazicisi;
        try {
            dosyaYazicisi = new FileWriter("cikti.txt");

            dosyaYazicisi.write(yazmacObegi.toString());

            dosyaYazicisi.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

}
