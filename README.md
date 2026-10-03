# 頑シミュ MHXX Android版

モンスターハンターダブルクロス（MHXX）向けスキルシミュレーター Android移植版

オリジナル：頑シミュ ver.0.9（masax_mh 様作）

---

## 機能

### スキルシミュレータ
- 欲しいスキルを複数選択して防具の組み合わせを検索
- 剣士 / ガンナー切り替え
- 男性 / 女性 / 両方の絞り込み

### **要求護石検索**（頑シミュ特有機能）
- 発動させたいスキルから**必要な護石を逆算**して検索
- 「どんなお守りがあれば発動できるか」を一覧表示
- 入手確率（MH4G換算 / 古参換算）も表示

### 除外装備設定
- 検索から外したい防具を登録

### 固定装備設定
- 必ず使用したい防具を固定

### 装飾品除外設定
- 持っていない・使わない装飾品を除外

### お守り設定
- 現在持っているお守りの情報を登録して検索に反映

### マイセット
- 気に入った組み合わせを最大999件保存

---

## ビルド方法

### GitHub Actionsで自動ビルド
1. このリポジトリをGitHubにプッシュ
2. Actions タブから `Build GanSimu MHXX APK` ワークフローを実行
3. Artifacts から APK をダウンロード

### ローカルビルド
```bash
# Debug APK
./gradlew assembleDebug

# Release APK（未署名）
./gradlew assembleRelease
```

**必要環境:**
- Java 17+
- Android SDK（Android Studio推奨）
- minSdk 21（Android 5.0+）

APKは `app/build/outputs/apk/` に出力されます。

---

## データについて

`app/src/main/assets/data/` 以下のCSVファイルがゲームデータです。
全てUTF-8に変換済みです。

| ファイル | 内容 |
|---------|------|
| MHXX_SKILL.csv | スキルデータ |
| MHXX_EQUIP_HEAD/BODY/ARM/WST/LEG.csv | 防具データ |
| MHXX_DECO.csv | 装飾品データ |
| MHXX_CHARM.csv | 護石データ |
| conf/CATEGORY.txt | スキルカテゴリ |
| conf/FUKUGO.txt | 複合スキル |
| conf/KEI.txt | 系統・カテゴリ分類 |
| conf/SIBORI.txt | 装備名の絞り込みパターン |

---

## オリジナルからの変更点

- Java Swing UI → Android Kotlin ネイティブ UI
- ダークテーマ（オリジナルに合わせた配色）
- データ保存：SharedPreferences（設定・マイセット）
- CSVエンコード：Shift-JIS → UTF-8

---

## ライセンス

ゲームデータおよびシミュレーターのロジックはオリジナル作者（masax_mh様）の著作物です。
本ポートは個人利用・学習目的のみを想定しています。
