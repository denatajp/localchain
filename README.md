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




## Apa Yang Perlu Disiapkan
- **JDK 8+**
- **Library Bouncy Castle** (untuk kriptografi ECDSA)
- **IDE** (Netbeans, VSCode, dll)
## Modifikasi
Kami menggunakan framework ONE Simulator untuk melakukan proses simulasi. Untuk itu, kami memodifikasi beberapa class dan juga membuat class baru untuk melengkapi algoritma. Secara struktur, perubahan yang kami buat adalah sebagai berikut:
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
|    └── TrannsactionEventGenerator.java
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

## Instalasi
Kami mengembangkan project ini menggunakan Netbeans sebagai IDE kami. Untuk instalasi project menggunakan Netbeans sebagai berikut:

1. Buat project baru, setelah itu pergi ke direktori tempat project berada (masuk folder src):
![image](https://github.com/user-attachments/assets/7d666f4a-3389-4791-a9c0-9e445066a324)

2. Clone Repository di direktori lewat Git Bash:
   ```bash
   git clone https://github.com/2denata/localchain.git
   ```

3. Pindahkan semua file dari folder `localchain` ke luar folder. Pastikan semua file dan folder ada di direktori `.../src/`

4. Tambahkan library dan .jar yang dibutuhkan:
Libraries = JUnit 4.12
JAR = "DTNConsoleConnection.jar" dan "ECLA.jar"
![image](https://github.com/user-attachments/assets/1eaeb3bb-c42b-419b-b5fe-eba4afab40bd)

5. Download .jar tambahan (Bouncy Castle)
Library ini penting untuk proses kriptografi. Download dari [link ini](https://www.bouncycastle.org/download/bouncy-castle-java/#latest)

6. Tambahkan bcprov-jdk18on-xxx.jar ke library

7. Edit custom configuration running project:
![image](https://github.com/user-attachments/assets/572d6c23-6ccf-4746-9943-1b8bd92df174)

8. Run Project!
