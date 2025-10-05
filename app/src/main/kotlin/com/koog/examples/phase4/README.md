# Phase 4: Model Context Protocol (MCP) Integration

このフェーズでは、Koog 0.4.1のMCP機能を使用して、Apidog MCPサーバーとChrome DevTools MCPサーバーとの統合を実装しています。

## 🎯 実装内容

### 1. Apidog MCP統合
- Apidog MCPサーバーとの連携
- API仕様の取得と管理
- APIエンドポイントのテスト機能
- 日本語対応のAPI質問応答システム

### 2. Chrome DevTools MCP統合
- ブラウザ自動化（ナビゲーション、クリック、フォーム入力）
- パフォーマンス分析とトレース
- ネットワークリクエスト監視
- スクリーンショット/スナップショット取得
- JavaScriptの実行とデバッグ
- コンソールログの確認
- レスポンシブデザイン検証

### 3. 技術的な実装
- Koog 0.4.1の`McpToolRegistryProvider`を使用
- Spring Bootとの統合
- RESTful APIエンドポイントの提供
- Gemini AIモデルによるインテリジェントな応答
- Docker内でChromeを自動セットアップ

## 📁 ファイル構成

### Apidog統合
- `ApidogMcpService.kt` - Apidog MCP統合のメインサービス
- `ApidogMcpController.kt` - REST APIコントローラー

### Chrome DevTools統合
- `ChromeDevToolsMcpService.kt` - Chrome DevTools MCP統合サービス
- `ChromeDevToolsMcpController.kt` - REST APIコントローラー

- `README.md` - このドキュメント

## 🔧 設定

### 必要な環境変数
```bash
# Google AI (Gemini) APIキー（必須）
GOOGLE_API_KEY=your_google_api_key_here

# Apidog設定（Apidog MCP使用時）
APIDOG_ACCESS_TOKEN=your_apidog_access_token_here
APIDOG_PROJECT_ID=your_apidog_project_id_here
```

### システム要件
- **Node.js**: v20.19以上（Docker内に自動インストール）
- **Chrome Browser**: Docker内に自動インストール済み（`google-chrome-stable`）
- ホスト側にChromeをインストールする必要はありません

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

---

## 🔍 Chrome DevTools MCP - 詳細情報

### 利用可能なツール (25種類)

#### 入力自動化 (7ツール)
- `click` - 要素をクリック
- `drag` - ドラッグ操作
- `fill` - フォーム入力
- `fill_form` - フォーム一括入力
- `handle_dialog` - ダイアログ処理
- `hover` - ホバー操作
- `upload_file` - ファイルアップロード

#### ナビゲーション (6ツール)
- `close_page` - ページを閉じる
- `list_pages` - 開いているページ一覧
- `navigate_page` - ページ遷移
- `new_page` - 新しいページを開く
- `select_page` - ページを選択
- `wait_for` - 要素の待機

#### エミュレーション (3ツール)
- `emulate_cpu` - CPU制限のエミュレーション
- `emulate_network` - ネットワーク制限のエミュレーション
- `resize_page` - ビューポートサイズ変更

#### パフォーマンス (3ツール)
- `performance_analyze_insight` - パフォーマンス分析
- `performance_start_trace` - トレース開始
- `performance_stop_trace` - トレース終了

#### ネットワーク (2ツール)
- `get_network_request` - 特定のリクエスト取得
- `list_network_requests` - リクエスト一覧

#### デバッグ (4ツール)
- `evaluate_script` - JavaScriptの実行
- `list_console_messages` - コンソールメッセージ取得
- `take_screenshot` - スクリーンショット撮影
- `take_snapshot` - DOMスナップショット取得

### Chrome DevTools使用例

#### スクリーンショット撮影
```bash
curl -X POST http://localhost:8080/phase4/chrome-devtools/execute \
  -H "Content-Type: application/json" \
  -d '{"task": "https://example.comにアクセスしてスクリーンショットを撮ってください"}'
```

**注意**: 現在、スクリーンショット画像はAIエージェント内部で処理されており、直接画像ファイルとして取得することはできません。

#### パフォーマンス分析
```bash
curl -X POST http://localhost:8080/phase4/chrome-devtools/execute \
  -H "Content-Type: application/json" \
  -d '{"task": "https://example.comのページ読み込みパフォーマンスを分析してください"}'
```

#### ネットワークリクエスト監視
```bash
curl -X POST http://localhost:8080/phase4/chrome-devtools/execute \
  -H "Content-Type: application/json" \
  -d '{"task": "https://example.comにアクセスして、すべてのネットワークリクエストをリストアップしてください"}'
```

#### JavaScript実行
```bash
curl -X POST http://localhost:8080/phase4/chrome-devtools/execute \
  -H "Content-Type: application/json" \
  -d '{"task": "https://example.comにアクセスして、ページタイトル(document.title)を取得してください"}'
```

---

## ⚠️ 既知の制限事項

### Apidog MCP
1. **MCP直接接続**: Apidog MCPサーバーが起動時に出力するメッセージがJSON-RPCプロトコルを妨害
2. **回避策**: AIエージェントベースの実装により、実用的には問題なく動作

### Chrome DevTools MCP
1. **スクリーンショット取得**: 画像データはAIエージェント内部で処理されており、直接画像ファイルとして取得不可
2. **ヘッドレスモード**: 現在はヘッドレスモードのみサポート
3. **同時実行**: 現在は1つのChromeインスタンスのみサポート

## 📚 参考資料

- [Koog MCP Documentation](https://github.com/JetBrains/koog/tree/main/agents/agents-mcp)
- [Apidog MCP Server Documentation](https://docs.apidog.com/jp/apidog-mcp-server)
- [Model Context Protocol Specification](https://modelcontextprotocol.io/)
