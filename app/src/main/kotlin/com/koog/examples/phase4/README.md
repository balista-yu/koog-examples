# Phase 4: Model Context Protocol (MCP) Integration

このフェーズでは、KoogのMCP (Model Context Protocol) 統合機能を実装します。

## 🎯 実装内容

### 1. 基本的なMCP統合
- SSE (Server-Sent Events) トランスポートを使用したMCP接続
- MCPツールのKoogエージェントへの統合
- 非同期処理とエラーハンドリング

### 2. MCPツール管理
- MCPツールレジストリの作成と管理
- ツールの動的登録と実行
- ツールのメタデータ管理

### 3. リソース管理
- MCPリソースの取得と管理
- リソースのキャッシング戦略
- リソースの更新通知

### 4. プロンプト戦略
- MCPプロンプトテンプレートの活用
- 動的プロンプト生成
- コンテキスト管理

## 📁 ファイル構成

- `McpBasicExample.kt` - 基本的なMCP統合の例
- `McpToolIntegration.kt` - MCPツール統合の実装
- `McpResourceManager.kt` - リソース管理の実装
- `McpPromptStrategy.kt` - プロンプト戦略の実装
- `McpServer.kt` - テスト用MCPサーバー実装

## 🚀 実行方法

```bash
# MCPサーバーの起動（別ターミナル）
./gradlew :app:runMcpServer

# MCPクライアントの実行
./gradlew :app:runPhase4Examples
```

## 📚 参考資料

- [Koog MCP Documentation](https://docs.koog.ai/model-context-protocol/)
- [Model Context Protocol Specification](https://modelcontextprotocol.io/)