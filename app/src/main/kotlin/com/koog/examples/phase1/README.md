# Phase 1: 基礎編

## 概要

このサンプルは、Koog 0.3.0とSpring Bootの基本的な統合方法を示します。
Google AI (Gemini)を使用したシンプルなエージェントの実装から始めて、RESTful APIの構築までを学習します。

## 実装されているエージェント

### 1. HelloWorldAgent
- Google AI (Gemini) との基本的な連携
- 固定のプロンプトに対するAI応答の取得
- 最もシンプルなエージェントの実装例

### 2. ChatAgent
- ユーザーからのメッセージを処理
- RESTful APIエンドポイントの実装
- JSON形式でのリクエスト/レスポンス処理

## セットアップ

### 1. 環境変数の設定

`.env`ファイルに以下のAPIキーを設定してください：

```bash
# Google AI API Key (必須)
GOOGLE_API_KEY=your_google_api_key_here

# オプション設定
AGENT_MODEL=gemini-2.0-flash-001
AGENT_SYSTEM_PROMPT="You are a helpful assistant. Please respond in Japanese."
```

### 2. アプリケーションの起動

```bash
# Dockerを使用する場合
task up

# または直接実行
./gradlew bootRun
```

## API エンドポイント

### ヘルスチェック
```bash
GET /api/agents/health
```

### HelloWorldAgent
```bash
GET /api/agents/hello
```

### ChatAgent
```bash
POST /api/agents/chat
Content-Type: application/json

{
  "message": "Kotlinについて教えて"
}
```

## 使用例

### HelloWorldAgent
```bash
curl http://localhost:8080/api/agents/hello
```

レスポンス例：
```json
{
  "status": "success",
  "response": "こんにちは！私はAIアシスタントです..."
}
```

### ChatAgent
```bash
curl -X POST http://localhost:8080/api/agents/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Kotlinについて教えて"}'
```

レスポンス例：
```json
{
  "status": "success",
  "message": "Kotlinについて教えて",
  "response": "Kotlinは、JetBrainsが開発した..."
}
```

## プロジェクト構造

```
phase1/
├── agent/
│   ├── HelloWorldAgent.kt   # 基本的なエージェント
│   └── ChatAgent.kt         # チャット機能を持つエージェント
├── config/
│   └── AgentConfig.kt       # エージェント設定（data class）
├── controller/
│   └── AgentController.kt   # REST APIコントローラ
├── dto/
│   ├── ChatRequest.kt       # リクエストDTO
│   └── ChatResponse.kt      # レスポンスDTO
└── README.md               # このファイル
```

## 技術的なポイント

### 1. エージェントの基本実装
- `simpleGoogleAIExecutor`を使用したGoogle AI統合
- `suspend`関数によるKotlin Coroutinesの活用
- Spring Bootの`@Component`によるDI

### 2. 設定管理
- `@ConfigurationProperties`を使用した設定の外部化
- data classによる型安全な設定管理
- 環境変数との連携

### 3. REST API実装
- Spring Bootの`@RestController`
- `runBlocking`を使用した非同期処理の同期化
- DTOパターンによるデータ転送

### 4. エラーハンドリング
- try-catchによる例外処理
- ユーザーフレンドリーなエラーメッセージ
- HTTPステータスコードの適切な使用

## 学習のポイント

1. **Koog Agentsの基本**
   - Executorの作成と設定
   - プロンプトの送信と応答の受信

2. **Spring Boot統合**
   - Beanの定義と依存性注入
   - 設定の外部化パターン

3. **Kotlin Coroutines**
   - suspend関数の使用
   - runBlockingによるブリッジ

## 次のステップ

Phase 2では、カスタムツールの作成と外部API連携について学習します：
- ツールの定義と実装
- ToolRegistryへの登録
- 外部サービスとの統合
