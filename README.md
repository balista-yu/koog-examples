# 🚀 Koog Examples

KoogフレームワークのSpring Boot統合サンプル集

## 🛠️ 技術構成

- **フレームワーク**: Spring Boot 3.5.3
- **言語**: Kotlin 2.1.21
- **AIライブラリ**: Koog Agents 0.4.1
- **LLMプロバイダー**: Google AI (Gemini 2.0 Flash)
- **ビルドツール**: Gradle 8.12.1
- **コンテナ**: Docker + Docker Compose
- **Java**: OpenJDK 21

## 🏗️ プロジェクト構成

```
koog-examples/
├── app/
│   ├── src/main/kotlin/com/koog/examples/
│   │   ├── Application.kt         # メインアプリケーション
│   │   ├── phase1/               # 基本的なKoogエージェント
│   │   ├── phase2/               # ツール開発・統合
│   │   ├── phase3/               # エージェント戦略（未実装）
│   │   └── phase4/               # MCP統合（Apidog）
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
```

必要な環境変数：
- `GOOGLE_API_KEY` - Google AI (Gemini) APIキー（必須）
- `OPENWEATHER_API_KEY` - OpenWeatherMap APIキー（Phase2用）
- `NEWS_API_KEY` - News APIキー（Phase2用）
- `APIDOG_ACCESS_TOKEN` - Apidog アクセストークン（Phase4用）
- `APIDOG_PROJECT_ID` - Apidog プロジェクトID（Phase4用）

### 4. 起動
```bash
# Taskを使用する場合
task up

# Docker Composeを直接使用する場合
docker-compose up -d
```

### 5. 動作確認

#### Phase 1: 基本的なKoogエージェント
- http://localhost:8080/phase1/hello - シンプルなエージェント
- http://localhost:8080/phase1/chat - チャットエージェント

#### Phase 2: ツール開発・統合
- http://localhost:8080/phase2/weather/{city} - 天気情報取得
- http://localhost:8080/phase2/news - ニュース取得
- http://localhost:8080/phase2/execute - ツール実行

#### Phase 4: MCP統合（Apidog）
- http://localhost:8080/phase4/apidog/status - ステータス確認
- http://localhost:8080/phase4/apidog/query - API質問応答
- http://localhost:8080/phase4/apidog/examples - サンプルクエリ

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
