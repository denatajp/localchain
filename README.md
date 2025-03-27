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
