# Phase 4: Model Context Protocol (MCP) Integration with Apidog

このフェーズでは、Koog 0.4.1のMCP機能を使用してApidog MCPサーバーとの統合を実装しています。

## 🎯 実装内容

### 1. Apidog MCP統合
- Apidog MCPサーバーとの連携
- API仕様の取得と管理
- APIエンドポイントのテスト機能
- 日本語対応のAPI質問応答システム

### 2. 技術的な実装
- Koog 0.4.1の`McpToolRegistryProvider`を使用
- Spring Bootとの統合
- RESTful APIエンドポイントの提供
- Gemini AIモデルによるインテリジェントな応答

## 📁 ファイル構成

- `ApidogMcpService.kt` - Apidog MCP統合のメインサービス
- `ApidogMcpController.kt` - REST APIコントローラー
- `README.md` - このドキュメント

## 🔧 設定

### 必要な環境変数
```bash
# Google AI (Gemini) APIキー（必須）
GOOGLE_API_KEY=your_google_api_key_here

# Apidog設定（Phase4で必要）
APIDOG_ACCESS_TOKEN=your_apidog_access_token_here
APIDOG_PROJECT_ID=your_apidog_project_id_here
```

### Apidogアクセストークンの取得方法
1. [Apidog](https://www.apidog.com/)にログイン
2. アカウント設定からアクセストークンを生成
3. プロジェクトIDを確認

## 🚀 実行方法

### アプリケーションの起動
```bash
# Dockerコンテナで起動
task up

# または
docker compose up
```

### APIエンドポイント

#### ステータス確認
```bash
curl http://localhost:8080/phase4/apidog/status
```

#### API質問応答
```bash
# 日本語での質問
curl -X POST http://localhost:8080/phase4/apidog/query \
  -H "Content-Type: application/json" \
  -d '{"query": "ペットの詳細を取得する場合何が取得できる?"}'

# 英語での質問
curl -X POST http://localhost:8080/phase4/apidog/query \
  -H "Content-Type: application/json" \
  -d '{"query": "What APIs are available?"}'
```

#### API仕様取得
```bash
curl http://localhost:8080/phase4/apidog/api-spec/user-service
```

#### APIエンドポイントテスト
```bash
curl -X POST http://localhost:8080/phase4/apidog/test-endpoint \
  -H "Content-Type: application/json" \
  -d '{
    "endpoint": "/users",
    "method": "POST",
    "body": "{\"name\": \"John Doe\", \"email\": \"john@example.com\"}"
  }'
```

#### サンプルクエリ一覧
```bash
curl http://localhost:8080/phase4/apidog/examples
```

## 💡 使用例

### ペットストアAPIの例

#### 新しいペットを登録
```bash
curl -X POST http://localhost:8080/phase4/apidog/query \
  -H "Content-Type: application/json" \
  -d '{"query": "新しいペットを登録するにはどのエンドポイントを使えばいい？"}'
```

#### ペット情報の取得
```bash
curl -X POST http://localhost:8080/phase4/apidog/query \
  -H "Content-Type: application/json" \
  -d '{"query": "ペットの詳細を取得するAPIのレスポンス形式を教えて"}'
```

## 🔍 技術的な詳細

### MCP統合の現状
- Apidog MCPサーバーは`npx`経由で起動
- プロトコル互換性の課題により、直接的なMCP接続は現在制限あり
- AIエージェント（Gemini）を使用した実用的な実装で動作

### アーキテクチャ
```
Client → REST API → ApidogMcpController → ApidogMcpService → AI Agent (Gemini)
                                                ↓
                                         Apidog MCP Server
```

## ⚠️ 既知の制限事項

1. **MCP直接接続**: Apidog MCPサーバーが起動時に出力するメッセージがJSON-RPCプロトコルを妨害
2. **回避策**: AIエージェントベースの実装により、実用的には問題なく動作

## 📚 参考資料

- [Koog MCP Documentation](https://github.com/JetBrains/koog/tree/main/agents/agents-mcp)
- [Apidog MCP Server Documentation](https://docs.apidog.com/jp/apidog-mcp-server)
- [Model Context Protocol Specification](https://modelcontextprotocol.io/)