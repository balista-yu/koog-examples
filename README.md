# 🚀 Koog Examples

KoogフレームワークのSpring Boot統合サンプル集

## 📋 サンプル一覧

### Phase 1: 基礎編 ✅
- [x] **HelloWorldAgent**: Google AI (Gemini) との基本連携
- [x] **ChatAgent**: メッセージ処理とRESTful API
- [x] **AgentConfig**: data classによる設定管理
- [x] **REST Endpoints**: JSONレスポンス

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
├── app/src/main/kotlin/com/koog/examples/
│   ├── Application.kt                    # メインアプリケーション
│   ├── phase1/
│   │   ├── config/AgentConfig.kt         # エージェント設定 (data class)
│   │   ├── basic/HelloWorldAgent.kt      # 基本エージェント
│   │   ├── examples/ChatAgent.kt         # チャットエージェント
│   │   ├── controller/AgentController.kt # REST API コントローラ
│   │   └── dto/                          # データ転送オブジェクト
│   │       ├── ChatRequest.kt
│   │       └── ChatResponse.kt
│   └── resources/application.yaml        # Spring設定
├── compose.yaml                          # Docker Compose設定
├── Taskfile.yaml                         # Task定義
└── README.md
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
GOOGLE_API_KEY=your_google_api_key
APP_ENV=dev
LOG_LEVEL=INFO
AGENT_MODEL=gemini-2.0-flash-001
AGENT_SYSTEM_PROMPT="You are a helpful assistant. Please respond in Japanese."
```

### 4. 起動
```bash
# Taskを使用する場合
task up

# Docker Composeを直接使用する場合
docker-compose up -d
```

### 5. 動作確認
```bash
# ヘルスチェック
curl http://localhost:8080/api/agents/health

# HelloWorldAgent (基本的なAI応答)
curl http://localhost:8080/api/agents/hello

# ChatAgent (任意のメッセージ処理)
curl -X POST http://localhost:8080/api/agents/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "こんにちは"}'
```

## 📡 API エンドポイント

### 基本エージェント
- `GET /api/agents/health` - サービス状態確認
- `GET /api/agents/hello` - HelloWorldAgent実行

### チャットエージェント
- `POST /api/agents/chat` - JSON形式のチャット (プログラム用)

### リクエスト例
```json
{
  "message": "Kotlinについて教えて"
}
```

### レスポンス例
```json
{
  "status": "success",
  "message": "Kotlinについて教えて",
  "response": "Kotlinは..."
}
```

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
