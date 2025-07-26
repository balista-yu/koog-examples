# 🚀 Koog Examples

KoogフレームワークのSpring Boot統合サンプル集

## 🛠️ 技術構成

- **フレームワーク**: Spring Boot 3.5.3
- **言語**: Kotlin 2.1.21
- **AIライブラリ**: Koog Agents 0.3.0
- **LLMプロバイダー**: Google AI (Gemini 2.0 Flash)
- **ビルドツール**: Gradle 8.11.1
- **コンテナ**: Docker + Docker Compose
- **Java**: OpenJDK 21

## 🏗️ プロジェクト構成

```
koog-examples/
├── app/
│   ├── src/main/kotlin/com/koog/examples/
│   │   ├── Application.kt         # メインアプリケーション
│   │   ├── phaseXX/               
│   └── src/main/resources/
│       └── application.yaml      # Spring設定
├── compose.yaml                  # Docker Compose設定
├── Taskfile.yaml                # Task定義
├── .env.example                 # 環境変数サンプル
└── README.md                    # このファイル
```

## 🚀 クイックスタート

### 1. 前提条件
- Docker & Docker Compose
- Task (推奨) 

### 2. 環境構築
```bash
git clone https://github.com/balista-yu/koog-examples.git
cd koog-examples
```

### 3. 環境変数設定
`.env`ファイルを作成：
```bash
cp .env.example .env
# 必要なAPIキーを設定
```

詳細は各Phaseのドキュメントを参照してください。

### 4. 起動
```bash
# Taskを使用する場合
task up

# Docker Composeを直接使用する場合
docker-compose up -d
```

### 5. 動作確認

各Phaseには独自のエンドポイントがあります。詳細は各PhaseのREADMEを参照してください。

## 🔧 開発コマンド

### Task使用（推奨）
```bash
task                # 使用可能なコマンド一覧
task build          # Dockerイメージビルド
task up             # アプリケーション起動
task down           # アプリケーション停止
task logs           # ログ確認
task test           # テスト実行
task clean          # クリーンアップ
```

### Gradle使用
```bash
./gradlew bootRun   # アプリケーション起動
./gradlew build     # ビルド
./gradlew test      # テスト実行
./gradlew clean     # クリーンアップ
```

## 📚 参考資料

- [Koog公式サイト](https://docs.koog.ai/)
- [Koog GitHub](https://github.com/JetBrains/koog)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
