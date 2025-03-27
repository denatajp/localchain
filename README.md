# Blockchain Transaction Mechanism in Delay-Tolerant Network (DTN)

Proyek ini mengimplementasikan mekanisme transaksi blockchain dalam jaringan Delay-Tolerant Network (DTN) dengan simulasi node miner, operator proxy, home, collector, dan internet. Sistem ini dirancang untuk menangani transaksi, penambangan blok, verifikasi konsensus, dan integrasi ke blockchain utama dalam lingkungan dengan koneksi yang terputus-putus.

![title](https://github.com/user-attachments/assets/18f1c180-a67a-4a0a-a02e-5b266e265793)

Mekanisme ini menggunakan konsep `Localchain`, di mana ada blockchain-blockchain lokal yang dimiliki oleh `Operator Proxy` di setiap area. `Localchain` ini yang akan digunakan dalam proses mining di setiap area sehingga nanti user yang berada di tiap area yang mengalami gangguan bisa bertransaksi bahkan bisa ikut serta dalam proses penambangan sebuah blok, dan nantinya bisa mendapatkan fee reward dari hasil penambangan mereka.


## Fitur Utama
- **Pembangkitan Transaksi**: Transaksi diamankan dengan tanda tangan digital ECDSA dan hash SHA-256.
- **Penambangan Blok**: Algoritma Proof-of-Work (PoW) dengan penyesuaian kesulitan (difficulty).
- **Konsensus dan Verifikasi**: Validasi blok oleh miner dengan threshold tertentu.
- **Localchain dan Mainchain**: Penyimpanan rantai blok sementara (localchain) dan penambahan ke blockchain utama.
- **Reward System**: Distribusi reward ke miner berdasarkan kontribusi penambangan.


## Instalasi
Kami mengembangkan project ini menggunakan Netbeans sebagai IDE kami. Untuk instalasi project menggunakan Netbeans sebagai berikut:

1. Buat project baru, setelah itu pergi ke direktori tempat project berada (masuk folder src):
![image](https://github.com/user-attachments/assets/7d666f4a-3389-4791-a9c0-9e445066a324)

2. Clone Repository di direktori lewat Git Bash:
   ```bash
   git clone https://github.com/2denata/localchain.git
   ```

3. Pindahkan semua file dari folder `localchain` ke luar folder. Pastikan semua file dan folder ada di direktori `.../src/`


4. Tambahkan library dan .jar yang dibutuhkan: <br>
![image](https://github.com/user-attachments/assets/1eaeb3bb-c42b-419b-b5fe-eba4afab40bd)
   - Libraries = JUnit 4.12
   - JAR = "DTNConsoleConnection.jar" dan "ECLA.jar"

5. Download .jar tambahan (Bouncy Castle)
Library ini penting untuk proses kriptografi. Download dari [link ini](https://www.bouncycastle.org/download/bouncy-castle-java/#latest)

6. Tambahkan bcprov-jdk18on-xxx.jar ke library

7. Edit custom configuration running project:
![image](https://github.com/user-attachments/assets/572d6c23-6ccf-4746-9943-1b8bd92df174)

8. Run Project!

## Aktor yang Terlibat

Berikut adalah entitas utama dalam simulasi blockchain DTN ini:


### 1. ![opeproxy](https://github.com/user-attachments/assets/44bcf68a-e832-4d55-bda3-cbd262ea20ae) **Miner (Penambang)** 
- **Peran**:  
  - Menambang blok baru dengan menyelesaikan Proof-of-Work (PoW).  
  - Menerima dan memverifikasi transaksi dari pengguna.  
  - Berpartisipasi dalam konsensus verifikasi blok.  
- **Karakteristik**:  
  - Memiliki dompet kripto (`Wallet`) dengan pasangan kunci ECDSA.  
  - Bergerak acak dalam area tertentu (model `RandomArea`).  
  - Contoh ID: `miner1`, `miner2`, dll.  


### 2. ![miner](https://github.com/user-attachments/assets/63c2efac-478e-4197-a777-b771ec87a193) **Operator Proxy**  
- **Peran**:  
  - Mengumpulkan dan mengelompokkan transaksi dari Miner.  
  - Mengkoordinasikan proses penambangan blok ke Miner.  
  - Memilih blok tercepat untuk ditambahkan ke *localchain*.
  - Bertugas memberikan fee kepada miner terpilih
- **Karakteristik**:  
  - Memiliki buffer transaksi (`transactionBuffer`).  
  - Mengelola `localchain` (rantai blok sementara).  
  - Contoh ID: `ope1`, `ope2`, dll.  


### 3. ![3](https://github.com/user-attachments/assets/ead250cf-13b9-40c0-9428-ee1c8a3331d9) **Home**  
- **Peran**:  
  - Menyimpan *localchain* yang dikirim Operator Proxy.  
  - Bertindak sebagai node penyimpanan sementara sebelum data dikirim ke Collector.  
- **Karakteristik**:  
  - Posisi tetap (`StationaryMovement`).  
  - Memiliki daftar `storedLocalchains`.  
  - Contoh ID: `home1`.  


### 4. ![collector](https://github.com/user-attachments/assets/97276a04-c60d-4f7f-8a25-8973f3dcd13b) **Collector**  
- **Peran**:  
  - Memilih *localchain* terbaik (terpanjang) dari Home.  
  - Mengintegrasikan *localchain* ke blockchain utama di Internet.  
- **Karakteristik**:  
  - Bergerak cepat untuk efisiensi koordinasi.  
  - Contoh ID: `col1`.  


### 5. ![4](https://github.com/user-attachments/assets/29228f0a-275a-4dbf-95a7-98f4c9d1900a) **Internet**  
- **Peran**:  
  - Menyimpan blockchain utama (`mainChain`).  
  - Validasi akhir dan penyimpanan blok dari Collector.  
- **Karakteristik**:  
  - Posisi tetap di lokasi terpisah.  
  - Contoh ID: `inter1`.

## Alur Program
### Inisialisasi:

- Miner membuat transaksi dan mengirim ke Operator Proxy.
- Transaksi dikelompokkan menjadi paket oleh Operator Proxy.
- Penambangan Blok:
     Operator Proxy membagikan transaksi ke Miner dengan cara mendatangi satu-satu miner. Miner yang didatangi melakukan proses mining sambil dicatat waktunya oleh OperatorProxy

### Verifikasi:
- Blok divalidasi oleh Miner yang ada di area.
- Blok valid ditambahkan ke localchain.

### Penyimpanan ke Blockchain:
- Localchain disimpan ke Home, lalu dipilih oleh Collector.
- Collector menambahkan localchain ke blockchain utama.

### Reward:
- Miner menerima reward berdasarkan kontribusi penambangan.

## Apa Yang Perlu Disiapkan
- **JDK 8+**
- **Library Bouncy Castle** (untuk kriptografi ECDSA)
- **IDE** (Netbeans, VSCode, dll)
  
## Modifikasi
Kami menggunakan framework ONE Simulator untuk melakukan proses simulasi. Untuk itu, kami memodifikasi beberapa class dan juga membuat class baru untuk melengkapi algoritma. Untuk mempermudah pengembangan, kami akan menjabarkan mana saja class yang kami modifikasi saja (sisanya default dari framework ONE Simulator). Secara struktur, perubahan yang kami buat adalah sebagai berikut:
```bash
src/
├── Blockchain/
|   ├── SecureTransaction.java
|   ├── Transaction.java
|   ├── Wallet.java
|   ├── Block.java
|   ├── Blockchain.java
|   └── Localchain.java
|
├── core/
|    ├── DTNHost.java
|    └── SimScenario.java
|
├── data/
|    ├── overlay.png
|    └── Node/
|         ├── collector.png
|         ├── internet.png
|         ├── miner.png
|         └── opeproxy.png
|
├── gui/
|   └── playfield/
|       └── NodeGraphic.java
|
├── input/
|    ├── TransactionCreateEvent.java
|    └── TransactionEventGenerator.java
| 
├── movement/
|       ├── RandomArea.java
|       └── MovementModel.java
|
├── routing/
|    └── EpidemicDecisionRouterBlockchain.java
| 
└── blockchainDTN.txt
```

<details>
  <summary>Spoiler warning</summary>
  
  Spoiler text. Note that it's important to have a space after the summary tag. You should be able to write any markdown you want inside the `<details>` tag... just make sure you close `<details>` afterward.

  
  ```java
  System.out.print("Hello World");
  ```
  
</details>





