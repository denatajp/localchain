# Blockchain Transaction Mechanism in Delay-Tolerant Network (DTN)

Proyek ini mengimplementasikan mekanisme transaksi blockchain dalam jaringan Delay-Tolerant Network (DTN) dengan simulasi node miner, operator proxy, home, collector, dan internet. Sistem ini dirancang untuk menangani transaksi, penambangan blok, verifikasi konsensus, dan integrasi ke blockchain utama dalam lingkungan dengan koneksi yang terputus-putus.

![Subtajuk (1)](https://github.com/user-attachments/assets/20cd5dc7-5147-4c24-8449-195ab4e3450c)


Mekanisme ini menggunakan konsep `Localchain`, di mana ada blockchain-blockchain lokal yang dimiliki oleh `Operator Proxy` di setiap area. `Localchain` ini yang akan digunakan dalam proses mining di setiap area sehingga nanti user yang berada di tiap area yang mengalami gangguan bisa bertransaksi bahkan bisa ikut serta dalam proses penambangan sebuah blok, dan nantinya bisa mendapatkan fee reward dari hasil penambangan mereka.

## Fitur Utama
- **Pembangkitan Transaksi**: Transaksi diamankan dengan tanda tangan digital ECDSA dan hash SHA-256.
- **Penambangan Blok**: Algoritma Proof-of-Work (PoW) dengan penyesuaian kesulitan (difficulty).
- **Konsensus dan Verifikasi**: Validasi blok oleh miner dengan threshold tertentu.
- **Localchain dan Mainchain**: Penyimpanan rantai blok sementara (localchain) dan penambahan ke blockchain utama.
- **Reward System**: Distribusi reward ke miner berdasarkan kontribusi penambangan.

## Simulasi
![Subtajuk](https://github.com/user-attachments/assets/e2ef1405-5848-4589-a1f6-9dbc46e802b2)
Simulasi dilakukan pada daerah _post-disaster_ di mana koneksi internet di daerah tersebut _down_. Namun, user di daerah tersebut ingin melakukan transaksi dan ingin transaksinya tersimpan di Blockchain. Lebih lagi, beberapa client juga ingin ikut melakukan mining untuk mendapatkan fee. Oleh karena itu ada pihak yang hadir sebagai pengelola transaksi, dan daerah tersebut dibagi menjadi 8 area yang masing-masing akan di-_handle_ oleh pihak tersebut untuk diteruskan nanti ke daerah yang jaringannya normal sehingga akan ditambahkan ke Blockchain. 


## Aktor yang Terlibat

Berikut adalah entitas utama dalam simulasi blockchain DTN ini:


### 1. ![miner](https://github.com/user-attachments/assets/63c2efac-478e-4197-a777-b771ec87a193) **Miner (Penambang)** 
- **Peran**:  
  - Menambang blok baru dengan menyelesaikan Proof-of-Work (PoW).  
  - Menerima dan memverifikasi transaksi dari pengguna.  
  - Berpartisipasi dalam konsensus verifikasi blok.  
- **Karakteristik**:  
  - Pergerakannya random, namun dibatasi satu area saja.
  - Memiliki dompet kripto (`Wallet`) dengan pasangan kunci ECDSA.
  - Bergerak acak dalam area tertentu (model `RandomArea`).  
  - Contoh ID: `minerSatu`, `minerDua`, dll.  


### 2. ![opeproxy](https://github.com/user-attachments/assets/44bcf68a-e832-4d55-bda3-cbd262ea20ae) **Operator Proxy**  
- **Peran**:  
  - Mengumpulkan dan mengelompokkan transaksi dari Miner.  
  - Mengkoordinasikan proses penambangan blok ke Miner.  
  - Memilih blok tercepat untuk ditambahkan ke *localchain*.
  - Bertugas memberikan fee kepada miner terpilih
- **Karakteristik**:  
  - Pergerakannya hanya satu jalur, dari Home ke satu area yang dihandle saja
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
  - Pergerakannya satu jalur, dari Home ke Internet saja. 
  - Contoh ID: `col1`.  


### 5. ![4](https://github.com/user-attachments/assets/29228f0a-275a-4dbf-95a7-98f4c9d1900a) **Internet**  
- **Peran**:  
  - Menyimpan blockchain utama (`mainChain`).  
  - Validasi akhir dan penyimpanan blok dari Collector.  
- **Karakteristik**:  
  - Posisi tetap di lokasi terpisah.  
  - Contoh ID: `inter1`.

## Apa Yang Perlu Disiapkan
- **JDK 8+**
- **Library Bouncy Castle** (untuk kriptografi ECDSA)
- **IDE** (Netbeans, VSCode, dll)

## Instalasi
Kami mengembangkan project ini menggunakan Netbeans sebagai IDE kami. Untuk instalasi project menggunakan Netbeans sebagai berikut:

1. Buat project baru, setelah itu pergi ke direktori tempat project berada (masuk folder src):
![image](https://github.com/user-attachments/assets/7d666f4a-3389-4791-a9c0-9e445066a324)

2. Clone Repository di direktori lewat Git Bash:
   ```bash
   git clone https://github.com/denatajp/localchain.git
   ```

3. Pindahkan semua file dari folder `localchain` ke luar folder. Pastikan semua file dan folder ada di direktori `.../src/` sehingga file ada di direktori seperti di gambar ![image](https://github.com/user-attachments/assets/7388b91d-0bf0-4cac-90ad-f77932b2e48f)




4. Tambahkan library dan .jar yang dibutuhkan: <br>
![image](https://github.com/user-attachments/assets/1eaeb3bb-c42b-419b-b5fe-eba4afab40bd)
   - Add Library = JUnit 4.12
   - Add JAR/Folder = "DTNConsoleConnection.jar" dan "ECLA.jar"

5. Download .jar tambahan (Bouncy Castle)
Library ini penting untuk proses kriptografi. Download dari [link ini](https://www.bouncycastle.org/download/bouncy-castle-java/#latest)

6. Tambahkan bcprov-jdk18on-xxx.jar ke library menggunakan Add JAR/Folder

7. Edit custom configuration running project:
![image](https://github.com/user-attachments/assets/357a1413-e06b-44da-91d6-6d3888cf1185)
   - **Main Class** = Class dimana program berjalan, pada project ini menggunakan class main DTNSim dari package core
   - **Arguments** = Parameter tambahan untuk berjalannya program. Karakter "-b" menandakan running program tidak menggunakan GUI, hapus jika ingin menggunakan GUI. Angka "1" menunjukkan indeks run (run berapa kali), lalu blockchainDTN.txt menunjukkan file settings mana yang digunakan untuk menjalankan simulasi
   - **Working Directory** = Direktori dimana file simulasi berjalan. Arahkan ke direktori src tadi.
     
8. Run Project!
   ![image](https://github.com/user-attachments/assets/3751b4b4-71e7-4805-913c-eed3067a8366)


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

<details>
  <summary>Alur Program Mendalam</summary>
  
# Alur detail

## Pembangkitan Pesan
- Pada 10.000 s pertama, program memanggil class `TransactionEventGenerator` sehingga men-_trigger_ class `TransactionCreateEvent` untuk membuat pesan baru dengan tujuan pesan ke Operator Proxy. Lalu juga membuat transaksi `Transaction` dan transaksi tersebut dibungkus ke dalam property pesan sebagai _transaction_. Proses ini hanya terjadi di node Miner saja.
- Selama pembangkitan pesan, node Miner yang baru saja membuat sebuah pesan juga akan bergerak di areanya sambil mencari relay (miner lain) untuk mengirim pesan agar sampai ke tujuan (Operator Proxy).
- Begitu Operator Proxy bertemu miner yang membawa pesan yang berisi transaksi itu, maka akan menyimpan pesan tersebut, lalu mengambil _transaction_ di dalamnya dan dimasukkan ke list `transactionBuffer` milik Operator Proxy. Proses ini berlangsung terus-menerus sampai fase pembangkitan pesan selesai (default settings.txt adalah 10.000 s untuk pembangkitan pesan).

## Store-Carry-Forward
- Pada 5000 s berikutnya, saat fase pembangkitan pesan telah berhenti, pasti masih ada transaksi yang belum sampai ke Operator Proxy (asumsi transaksi baru dibuat sesaat sebelum fase pembangkitan pesan berhenti).
- Pada 5000 s ini, simulasi akan berfokus pada proses store-carry-forward agar pesan sampai ke Operator Proxy.
- Proses ini akan berhenti di waktu 15.000 s simulasi.

## Grouping Transaction
- Setelah melewati 15.000 s, maka sudah tidak ada pembangkitan pesan lagi, dan asumsi Operator Proxy sudah tidak menerima transaksi lagi. Lalu, 5000 s selanjutnya ini merupakan waktu untuk Operator Proxy mengelompokkan transaksi-transaksi di dalam `transactionBuffer` menjadi beberapa kelompok secara acak.
- Transaksi-transaksi yang ada di `transactionBuffer` ini akan dipisah-pisah, lalu dikelompokkan menjadi beberapa list transaksi. Kumpulan list transaksi ini akan disimpan ke dalam satu list yaitu `trx` milik Operator Proxy.
- Proses Grouping ini berjalan selama 5000 s sampai waktu simulasi mencapai 20.000. Saat sudah selesai melakukan _grouping_, Operator Proxy menandakan dirinya sudah selesai dengan mengeset `hasGrouped = true` pada dirinya sendiri untuk mengantisipasi terjadi pengulangan grouping selanjutnya.

## Mining
- Setelah 20.000 s simulasi, maka dimulai proses _mining_. Proses ini terjadi jika dan hanya jika node Miner bertemu dengan Operator Proxy saja.
- Saat Operator Proxy menemui sebuah miner, pertama-tama Operator Proxy harus mencatat kedatangan miner tersebut (seperti absensi) menggunakan HashSet bernama `getVisitedMiner` untuk menandai bahwa miner tersebut sudah pernah bertemu. 
- Selanjutnya miner akan melihat-lihat dahulu pada list `trx` milik Operator Proxy. Karena di dalam list `trx` terdapat list-list yang berisi kumpulan transaksi, maka miner bisa memilih kira-kira mana list yang memiliki _fee_ terbesar (1% dari total amount pada tiap transaksi). Jika sudah memilih, maka miner tersebut akan siap untuk melakukan pembuatan blok.
- Sebelum itu, miner akan mengecek keaslian data transaksi dengan cara mengecek tanda tangan digital pada transaksi yang dipilih menggunakan fungsi kriptografi pada _class_ `SecureTransaction`. Jika sudah valid, miner membungkus list transaksi yang dipilih ke dalam sebuah `Block` baru.
- Saat `Block` baru dibuat, miner akan siap untuk mencari nilai _nonce_ pada blok tersebut sesuai dengan target kesulitan pada Blockchain dan Localchain. Operator Proxy akan bertugas untuk mencatat waktu mulai miner melakukan _mining_ dan waktu selesai.
- Proses _mining_ berlangsung.
- Setelah selesai, waktu _mining_ dicatat ke dalam blok, dan blok yang telah di-_mining_ akan disimpan di penyimpanan sementara milik Operator Proxy yaitu `minedBlock`. Saat miner ini nanti bertemu Operator Proxy kembali, maka tidak akan melakukan _mining_ lagi karena sudah melakukan absen di `getVisitedMiner`.
- Operator Proxy lanjut mendatangi miner lain di areanya. Saat bertemu miner lain, prosesnya sama, namun karena list `trx` tidak ada yang berubah, nantinya miner lain pasti juga akan memilih list transaksi yang sama dengan miner pertama. Apakah salah? tidak, karena memang ini konsepnya, yakni Operator Proxy nanti akan membandingkan versi _mining_ antar miner untuk list transaksi yang sama, sehingga versi _mining_ dengan interval waktu mining tercepat akan dipilih.
- Saat satu list dari `trx` ini sudah di-_mining_ oleh semua miner di area tersebut (`getVisitedMiner == minersInGroup`: asumsi ada 7 miner/area), maka di sini Operator Proxy akan memilih blok dengan interval mining terbaik, lalu memasukkannya ke `selectedBlock`. Tidak lupa Operator Proxy juga akan remove list terpilih di `trx` agar tidak di-_mining_ lagi, dan juga mereset `minedBlock` dan `getVisitedMiner` untuk lanjut list di `trx` selanjutnya. Namun sebelum lanjut ke list selanjutnya, akan menjalankan proses verifikasi dahulu, karena jika Operator Proxy sedang memegang blok terpilih (`selectedBlock is not null`), maka tidak bisa melakukan proses _mining_ dulu dan harus diverifikasi dulu. 

## Verification
- Proses verifikasi dilakukan saat Operator Proxy sedang memegang blok terpilih (`selectedBlock is not null`), dan kembali akan mendatangi miner-miner lain dengan skema absensi `getVisitedMiner` seperti pada proses _mining_. 
- Saat bertemu miner, maka Operator Proxy memberikan blok terpilih untuk dilakukan verifikasi _hash_ pada blok. Apakah _hash_ dari blok tersebut sudah memenuhi kriteria/target kesulitan pada Blockchain atau tidak.
- Jika memenuhi target, maka Operator Proxy melakukan _increment_ nilai `v`, yaitu jumlah miner yang menyetujui blok terpilih.
- Proses verifikasi berlanjut ke miner-miner lain, sampai jumlah `v` sudah mencapai `threshold`, maka blok dianggap valid dimasukkan ke Localchain milik Operator Proxy.
- Setelah itu, Operator Proxy kembali mereset nilai `v`, absensi `getVisitedMiner`, dan mengubah status `selectedBlock` menjadi `null` untuk melakukan proses _mining_ kembali pada list `trx` selanjutnya.
- Operator Proxy akan melakukan proses _mining_-_verification_ terus-menerus secara berurutan sampai list `trx` di Operator Proxy kosong (`.getTrx().isEmpty()`) dan mengeset status Operator Proxy menjadi siap untuk melakukan _storing_ Localchain (`readyToStore = true`).

## Storing
- Proses ini dilakukan saat Operator Proxy bertemu dengan Home, dengan syarat bahwa Operator Proxy harus sudah menjalankan tugas di areanya (_mining_-_verification_; `readyToStore = true`).
- Home akan menggunakan skema absensi `getVisitedOperatorProxy` untuk mencatat kedatangan Operator Proxy. Jadi tiap Operator Proxy hanya bisa menyetor Localchain sekali saja.
- Setiap Operator Proxy yang datang ke Home akan menyetorkan Localchainnya ke `storedLocalchains` milik Home.
- Proses storing terus berjalan sampai semua Operator Proxy mengirim ke Home.

## Selection
- Setelah disetor, kemudian proses dilanjutkan dengan memilih Localchain terbaik. Proses selection ini dilakukan antara node Home dengan node Collector.
- Saat Collector mengunjungi Home, akan memeriksa `storedLocalchains` di Home untuk memilih Localchain terbaik dengan rantai (blok) terpanjang.
- Collector juga akan mengkalkulasi hash dari tiap Localchain. Hash yang dimaksud adalah nilai dari penggabungan _hash_ pada blok-blok di dalam localchain tersebut. Jadi sekarang tiap localchain punya hashnya sendiri-sendiri.
- Selanjutnya Collector memeriksa dalam list `storedLocalchains` menghitung nilai hash dari hexadecimal menjadi desimal. Localchain dengan hash terkecil akan dimasukkan ke variabel sementara `selected`.
- Collector kemudian mengambil `selected` tadi dan memasukkan ke penyimpanan miliknya yaitu `selectedLocalchain`. Lalu menghapus localchain terpilih dari list `storedLocalchains` milik Home tadi.
- Proses selection selesai, Collector sudah memegang satu localchain (`selectedLocalchain is not null`) dan siap pergi ke node Internet untuk menambahkan localchain terpilih ke Blockchain, dan jika Collector datang ke Home lagi sebelum berkunjung ke Internet, maka tidak akan melakukan apa-apa karena dia sedang memegang Localchain.

## Appending
- Proses _appending_ dilakukan oleh node Collector dan node Internet, dengan syarat Collector harus dan hanya memegang satu localchain terpilih dari Home.
- Internet akan memeriksa `selectedLocalchain` yang dibawa Collector dengan menghitung _hash_ nya terlebih dahulu lalu akan dibandingkan dengan _hash_ yang sudah ada. Jika valid, maka lanjut proses menambahkan localchain ke Blockchain.
- Proses penambahan ke Blockchain ini sedikit kompleks, karena harus kembali memecah blok-blok di dalam localchain lalu harus mengubah `previousHash` dari blok pertama pada Localchain (yang tadinya bernilai `null`) menjadi _hash_ dari blok terakhir/terbaru pada Blockchain. Perubahan `previousHash` ini akan mempengaruhi _hash_ dari semua blok di Localchain tersebut karena perubahan kecil saja akan mengubah keseluruhan _hash_ dan _hash_ yang baru belum tentu memenuhi target kesulitan pada Blockchain.
- Namun pada program ini, tiap blok sudah memiliki atribut `K` di mana menunjukkan asal blok tersebut dibuat dari mana. Jika bernilai 1 (`K = 1`), maka tandanya Block tersebut berasal dari Localchain dan sudah dipastikan valid. Sehingga walaupun _hash_ baru dari blok yang sudah diubah `previousHash`nya tadi tidak memenuhi `difficulty`, maka tetap akan dianggap valid karena blok berasal dari Localchain.
- Selanjutnya semua blok pada `selectedLocalchain` akan ditambahkan ke Blockchain menggunakan mekanisme di atas.
- Saat semua hash dari blok sudah valid, maka proses penambahan ke Blockchain sudah berhasil. Collector akan mengeset `selectedLocalchain` menjadi `null` dan siap untuk mengambil localchain lainnya yang tersisa di Home.
- Proses _selection_-_appending_ ini akan berlangsung terus-menerus secara berurutan sampai jumlah localchain yang ada habis `localChainCount is empty`, dan Collector akan menandai dirinya sendiri sudah selesai (set `appendingDone = true`).
- Semua transaksi sudah ditambahkan ke Blockchain, selanjutnya adalah memberikan reward ke masing-masing miner yang sudah berkontribusi.

## Reward
- Fase ini dilakukan oleh beberapa node. Pertama dari Collector yang `appendingDone = true` akan mengunjungi Home, lalu mengeset `appendingDone = true` juga di Home. Lalu saat OperatorProxy mengunjungi Home yang sudah menandai bahwa _appending_ sudah selesai, maka Operator Proxy juga akan menandai `appendingDone = true` pada dirinya sendiri.
- Selanjutnya Operator Proxy akan kembali mengunjungi miner-miner di area nya dan memeriksa berdasarkan list `localchain` yang masih disimpannya untuk memberikan reward.
- Saat `localchain` sudah kosong, maka tandanya Operator Proxy ini sudah selesai melakukan rewarding ke miner di areanya, lalu menandai dirinya sendiri sudah selesai (set `doneReward = true`). Lalu saat Operator Proxy yang sudah selesai melakukan tugasnya mengunjungi Home, maka Home akan mencatat kedatangan Operator Proxy tersebut di HashSet `confirmedDoneOperatorProxy`.
- Saat `confirmedDoneOperatorProxy` sudah memiliki `size == 8` maka tandanya semua Operator Proxy sudah selesai menajalankan tugasnya, dan mekanisme transaksi Blockchain sudah selesai.
  
</details>


  
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
|   ├── CLIMessageStatus.java
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
├── report/
|    └── StorageCapacityReport.java
| 
└── blockchainDTN.txt
```

<details>
  <summary>Detail</summary>
   
### SecureTransaction.java
Berperan dalam proses kriptografi. Implementasi hashing SHA256 serta pembuatan tanda tangan digital menggunankan ECDSA dan library Bouncy Castle.

### Transaction.java
Class Transaction dimana objek transaksi dibuat oleh para miner untuk bertransaksi.

### Wallet.java
Dompet digital yang dimiliki tiap miner. Tiap wallet memiliki Public Key dan Private Key.

### Block.java
Class Block yang merupakan class penting yang digunakan dalam mekanisme transaksi di Blockchain dimana membungkus transaksi-transaksi yang dibuat oleh miner.

### Blockchain.java
Blockchain digunakan sebagai rantai utama yang tersimpan di Internet.

### CLIMessageStatus.java
Class untuk membuat tampilan UI pada terminal

### Localchain.java
Blockchain lokal yang dimiliki oleh setiap Operator Proxy.

### DTNHost.java
Penambahan atribut tergantung pada apa rolenya (miner, Operator Proxy, Home, Collector atau Internet).

### SimScenario.java
Penambahan atribut "difficulty" untuk transaksi blockchain. Lalu inisialisasi Localchain tiap Operator Proxy dan inisialisasi Blockchain pada Internet saat menjalankan method createHosts().

### package data/
Penambahan overlay image untuk tampilan GUI Playlist yang lebih informatif.

### NodeGraphic.java
Modifikasi fontColor tiap role agar berbeda di GUI. Lalu modifikasi drawHost yang tadinya memanfaatkan drawRectangle menjadi drawImage untuk menampilkan node dalam bentuk gambar.

### TransactionCreateEvent.java
Modifikasi dari MessageCreateEvent, dimana penambahan property "transaction" di dalam message. Lalu memastikan hanya node miner saja yang dapat membangkitkan pesan.

### TransactionEventGenerator.java
Modifikasi dari MessageEventGenerator dimana menambahkan field eventCount yang akan selalu berubah-ubah untuk membuat ID unik transaksi. Mengatur tiap message final destinationnya hanya ke Operator Proxy pada area mereka. Lalu pembuatan transaksi, penandatanganan transaksi, serta pembungkusan transaksi dilakukan di sini.

### RandomArea.java
Modifikasi dari RandomWaypoint, perbedaannya adalah membatasi pergerakan randomnya menggunakan area sehingga tidak dapat bergerak diluar area yang ditentukan. Area diambil dari inputan user "moveArea" pada settings.

### MovementModel.java
Penambahan atribut "moveArea".

### StorageCapacityReport.java
Report untuk mencatat usage storage pada Operator Proxy per selang waktu tertentu.

### EpidemicDecisionRouterBlockchain.java
Algoritma Routing mekanisme transaksi blockchain DTN.
  
</details>
