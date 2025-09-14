# Phase 4: Model Context Protocol (MCP) Integration Simulation

このフェーズでは、将来のKoogのMCP (Model Context Protocol) 統合機能をシミュレートした実装を提供します。

**注意**: Koog 0.4.1では正式なMCPサポートがまだ含まれていないため、カスタムツールレジストリを使用してMCPのような機能を実現しています。

## 🎯 実装内容

### 1. MCPシミュレーション実装
- カスタムツールレジストリによるMCP機能のシミュレーション
- Koogエージェントとの統合
- 非同期処理とエラーハンドリング

### 2. シミュレートされたツール
- Echo Tool: メッセージのエコー
- Time Tool: 現在時刻の取得（ISO, Unix, ミリ秒）
- Calculator Tool: 基本的な計算（加減乗除）
- Data Fetch Tool: 外部データ取得のシミュレーション

## 📁 ファイル構成

- `McpSimulationExample.kt` - MCPシミュレーションの実装
- `McpSimulationController.kt` - REST APIコントローラー
- `README.md` - このドキュメント

## 🚀 実行方法

### Dockerコンテナで実行
```bash
# アプリケーションの起動
task up

# APIエンドポイントのテスト
curl http://localhost:8080/api/phase4/mcp/status
curl http://localhost:8080/api/phase4/mcp/tools
curl -X POST http://localhost:8080/api/phase4/mcp/simulate
```

### 直接実行
```bash
# Kotlinファイルの実行
docker exec koog-examples-app-container ./gradlew run --args="com.koog.examples.phase4.McpSimulationExampleKt"
```

## 📚 参考資料

- [Koog MCP Documentation](https://docs.koog.ai/model-context-protocol/)
- [Model Context Protocol Specification](https://modelcontextprotocol.io/)