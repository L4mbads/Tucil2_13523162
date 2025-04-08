<h1 align="center">Tugas Kecil 2 IF2211 Strategi Algoritma</h1>
<h3 align="center">Kompresi Gambar Dengan Metode Quadtree</p>


![output3](https://github.com/L4mbads/Tucil2_13523162/blob/c4ccd8b997808819c69d9fd97454096ba96233ce/test/output3.gif)


## Daftar Isi
- [Overview](#overview)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Credits](#credits)
- [Author](#author)

## Overview
Program ini adalah program sederhana dalam bahasa **Java** (CLI) yang mengimplementasikan algoritma _divide and conquer_ untuk melakukan kompresi gambar berbasis **quadtree** dengan parameter kompresi dari pengguna sepert:
- Metode perhitungan galat:
  - Varians
  - _Mean Absolute Deviation_
  - _Max Pixel Difference_
  - Entropi
  - _Structural Similarity Index Measure_ (SSIM)
- Ambang batas galat.
- Ukuran blok minimum.
- Target persentase kompresi (jika aktif, maka program akan mengatur nilai ambang batas galat secara dinamis).
- Opsi untuk mengekspor GIF.

Program dibuat dengan Java versi 21.
## Requirements
Pastikan Java Runtime Environment terpasang di sistem operasi agar bisa menjalankan program. Untuk mengkompilasi program, pastikan juga Java Development Kit terpasang.

## Installation
Untuk menjalankan program, maka lakukan langkah berikut:

1. Klon repositori ini ke lokal:
```shell
git clone https://github.com/L4mbads/Tucil2_13523162
```

2. Masuk ke repo lokal:
```shell
cd Tucil2_13523162
```

3. Untuk mengkompilasi program, gunakan salah satu dari skrip build sesuai dengan sistem operasi:
```shell
./build.sh  # jika menggunakan Linux
```
```pwsh
./build.bat # jika menggunakan Windows
```

4. Jalankan program:
```shell
java -jar bin/ImageCompressor.jar
```

Pastikan requirements terpenuhi sebelum menjalankan program.

## Usage
Setelah menjalakan program, pengguna harus memasukkan parameter-parameter berikut:
- Alamat absolut gambar yang akan dikompresi.
- Metode perhitungan galat.
- Ambang batas galat.
- Ukuran blok minimum.
- Target persentase kompresi (floating number, 1.0 = 100%), beri nilai 0 jika ingin menonaktifkan mode ini.
- Alamat absolut gambar hasil kompresi.
- Alamat absolut GIF (beri "n" untuk tidak membuat GIF).

Seluruh parameter dapat di-_buffer_. Artinya pengguna bisa memasukkan seluruh parameter sejak saat _input_ pertama saja, dipisah dengan spasi.

## Credits
- [dragon66](https://github.com/dragon66/animated-gif-writer) - Pustaka AnimatedGIFWriter.
- [MOKOLS](https://www.pixiv.net/en/users/13544932) - Gambar input pertama dan kedua.
- [Artem Saranin](https://www.pexels.com/photo/photo-of-a-pathway-in-a-forest-1496373/) - Gambar input ketiga.

## Author
- [Fachriza Ahmad Setiyono](https://github.com/L4mbads) - 13523162 - K3
