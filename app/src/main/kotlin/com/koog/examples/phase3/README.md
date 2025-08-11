# Phase 3: エージェント戦略・イベント駆動パターン

## 概要
Phase 3では、Koog v0.3.0の**EventHandler機能**と**StringSubgraphResult**を活用したイベント駆動型エージェントの実装例を提供します。

## 実装されているKoog機能

### 1. EventHandler機能 (`ai.koog.agents.features.eventHandler`)
- `handleEvents`ブロックでイベントコールバックを設定
- `onToolCall`：ツール呼び出し時のイベント処理
- `onAgentFinished`：エージェント完了時のイベント処理

### 2. StringSubgraphResult (`ai.koog.agents.ext.agent`)
- サブグラフ処理結果を文字列として管理
- パイプライン処理の実装
- 並列サブグラフ実行のサポート

## 実装されたエージェント

### SimpleEventAgent
最小限のEventHandler実装例
- Koog標準のEventHandler機能のみを使用
- シンプルなイベント追跡とロギング
- 初心者向けの分かりやすい実装

### KoogEventHandlerAgent
詳細なイベント管理の実装例
- 詳細なイベント追跡とメトリクス収集
- 並列タスク処理でのイベント管理
- メトリクス（ツール呼び出し回数など）の収集

### SubgraphAgent
サブグラフ機能の実装例
- `StringSubgraphResult`を使用したサブグラフ処理
- 3段階パイプライン（分析→処理→最終化）
- 並列サブグラフ実行と結果集約
- 複雑なワークフローの構築例

## APIエンドポイント

### Base URL: `/api/phase3`

| メソッド | パス | 説明 |
|---------|------|------|
| POST | `/simple-events` | シンプルなイベント処理 |
| POST | `/detailed-events` | 詳細なイベント追跡 |
| POST | `/parallel-events` | 並列タスク処理 |
| POST | `/subgraph` | サブグラフパイプライン処理 |
| POST | `/parallel-subgraphs` | 並列サブグラフ実行 |
| GET | `/info` | API情報の取得 |

## 使用例とレスポンス

### 1. シンプルなイベント処理
```bash
curl -X POST http://localhost:8080/api/phase3/simple-events \
  -H "Content-Type: application/json" \
  -d '{"message": "1+5は？"}'
```

**レスポンス例：**
```json
{
    "success": true,
    "message": "6\n",
    "events": [
        "2025-08-11T13:57:25.672600948: Processing started",
        "2025-08-11T13:57:26.606880971: Tool called: say_to_user",
        "2025-08-11T13:57:27.462217842: Agent finished",
        "2025-08-11T13:57:27.463185879: Processing completed in 1791ms"
    ],
    "duration": 1791
}
```

### 2. 詳細なイベント追跡
```bash
curl -X POST http://localhost:8080/api/phase3/detailed-events \
  -H "Content-Type: application/json" \
  -d '{"message": "1+5は？"}'
```

**レスポンス例：**
```json
{
    "success": true,
    "sessionId": "7be9a0fc-3fe7-402b-a157-ec7635e55592",
    "message": "6\n",
    "events": [
        {
            "timestamp": "2025-08-11T13:58:17.218119500",
            "type": "TOOL_CALL",
            "details": {
                "toolName": "say_to_user",
                "toolDescription": "Service tool, used by the agent to talk.",
                "arguments": "Args(message=6)",
                "callNumber": 1
            }
        },
        {
            "timestamp": "2025-08-11T13:58:18.018176805",
            "type": "AGENT_FINISHED",
            "details": {
                "result": "6\n",
                "metrics": {
                    "totalToolCalls": 1,
                    "totalLLMCalls": 0,
                    "finalResult": "6\n"
                }
            }
        }
    ],
    "metrics": {
        "duration": 1697,
        "toolCalls": 1,
        "llmCalls": 0,
        "eventCount": 2
    }
}
```

### 3. サブグラフパイプライン処理
```bash
curl -X POST http://localhost:8080/api/phase3/subgraph \
  -H "Content-Type: application/json" \
  -d '{"message": "Kotlinについて簡単に説明してください"}'
```

**レスポンス例：**
```json
{
    "success": true,
    "sessionId": "6d4b0355-8a82-4ce4-9b75-b6fbebb94cfd",
    "analysisResult": "分析結果：\n\n1.  主要なトピック：Kotlinプログラミング言語の説明。\n2.  必要なアクション：Kotlinに関する簡潔な説明を提供する。\n3.  期待される出力：Kotlinの主な特徴、用途、利点などを含む、わかりやすい説明。\n",
    "processingResult": "Kotlinプログラミング言語について説明します。Kotlinは、Java仮想マシン（JVM）上で動作する静的型付けのプログラミング言語で、Androidアプリ開発で広く使用されています。Javaとの互換性があり、簡潔で安全なコードを書くことができます。Kotlinの主な特徴は、null安全、関数型プログラミングのサポート、拡張関数、データクラスなどです。Androidアプリ開発以外にも、サーバーサイドやWebフロントエンドの開発にも利用できます。Javaに比べてコード量が削減でき、保守性も高いため、多くの開発者に採用されています。\n",
    "finalResult": "Kotlinは、Java仮想マシン(JVM)上で動作する静的型付けのプログラミング言語で、特にAndroidアプリ開発で広く使われています。Javaとの互換性があり、簡潔で安全なコードが書けるのが特徴です。\n\n**主な特徴:**\n\n*   **Null安全:** NullPointerExceptionを未然に防ぐ機能があります。\n*   **関数型プログラミングのサポート:** 関数を第一級オブジェクトとして扱えます。\n*   **拡張関数:** 既存のクラスに新しい機能を追加できます。\n*   **データクラス:** データ保持に特化した簡潔なクラスを定義できます。\n\n**用途:**\n\nAndroidアプリ開発のほか、サーバーサイドやWebフロントエンドの開発にも利用可能です。\n\n**利点:**\n\nJavaに比べてコード量が削減でき、保守性が高いため、多くの開発者に採用されています。\n\n**次のステップ:**\n\nKotlinについてさらに詳しく知りたい場合は、公式ドキュメントやチュートリアルを参照することをおすすめします。また、実際にコードを書いてみることで、Kotlinの利点をより深く理解できます。\n",
    "duration": 5733
}
```

### 4. 並列サブグラフ実行
**違い**: 各タスクを3段階のサブグラフ（分析→処理→最終化）で処理。StringSubgraphResultを使用。

```bash
curl -X POST http://localhost:8080/api/phase3/parallel-subgraphs \
  -H "Content-Type: application/json" \
  -d '{
    "tasks": [
      "Pythonとは何ですか？",
      "JavaScriptとは何ですか？",
      "Rustとは何ですか？"
    ]
  }'
```

**レスポンス例：**
```json
{
    "sessionId": "9849f6ef-bb56-4ce7-993e-493df674aef3",
    "totalTasks": 3,
    "taskResults": [
        {
            "taskId": "57d8864d-9288-46de-93fd-c2af461e0010",
            "taskIndex": 0,
            "task": "Pythonとは何ですか？",
            "result": "Pythonは、汎用性の高い高水準プログラミング言語です。コードの可読性を重視した設計になっており、インデントを使用してコードの構造を定義します。Pythonは、Web開発、データサイエンス、機械学習、スクリプト作成など、さまざまな用途に使用されています。豊富なライブラリとフレームワークがあり、大規模なコミュニティによるサポートも充実しています。\n",
            "duration": 1728
        },
        {
            "taskId": "8ee4e11a-143c-4803-995d-fb76c664ffdf",
            "taskIndex": 1,
            "task": "JavaScriptとは何ですか？",
            "result": "JavaScriptは、Webページをインタラクティブにするために使用されるプログラミング言語です。",
            "duration": 1164
        },
        {
            "taskId": "2f9d4b20-1916-4024-80fe-ce49ef7bc26c",
            "taskIndex": 2,
            "task": "Rustとは何ですか？",
            "result": "Rustは、安全性、速度、並行性に重点を置いたシステムプログラミング言語です。NULLポインタのデリファレンスやデータ競合などの一般的なプログラミングエラーを防ぐように設計されており、信頼性が高く高性能なソフトウェアを構築するのに適しています。\n",
            "duration": 2748
        }
    ],
    "aggregatedResult": "            === 並列サブグラフ実行結果 ===\n            合計タスク数: 3\n            \n            タスク 1:\n入力: Pythonとは何ですか？\n結果: Pythonは、汎用性の高い高水準プログラミング言語です。コードの可読性を重視した設計になっており、インデントを使用してコードの構造を定義します。Pythonは、Web開発、データサイエンス、機械学習、スクリプト作成など、さまざまな用途に使用されています。豊富なライブラリとフレームワークがあり、大規模なコミュニティによるサポートも充実しています。\n\n実行時間: 1728ms\n\nタスク 2:\n入力: JavaScriptとは何ですか？\n結果: JavaScriptは、Webページをインタラクティブにするために使用されるプログラミング言語です。\n実行時間: 1164ms\n\nタスク 3:\n入力: Rustとは何ですか？\n結果: Rustは、安全性、速度、並行性に重点を置いたシステムプログラミング言語です。NULLポインタのデリファレンスやデータ競合などの一般的なプログラミングエラーを防ぐように設計されており、信頼性が高く高性能なソフトウェアを構築するのに適しています。\n\n実行時間: 2748ms\n            \n            === サマリー ===\n            平均実行時間: 1880.0ms",
    "totalDuration": 2751
}
```

### 5. 並列タスク処理（イベント付き）
**違い**: 単純な並列実行で、各タスクのイベント（ツール呼び出し等）を追跡。サブグラフは使わない。

```bash
curl -X POST http://localhost:8080/api/phase3/parallel-events \
  -H "Content-Type: application/json" \
  -d '{"tasks": ["タスク1", "タスク2", "タスク3"]}'
```

**レスポンス例：**
```json
{
  "sessionId": "xyz-789",
  "totalTasks": 3,
  "completedTasks": 3,
  "results": [
    {
      "taskId": "id-1",
      "task": "タスク1",
      "result": "タスク1を完了しました",
      "duration": 1000,
      "toolCallCount": 1
    },
    {
      "taskId": "id-2",
      "task": "タスク2",
      "result": "タスク2を完了しました",
      "duration": 1200,
      "toolCallCount": 2
    },
    {
      "taskId": "id-3",
      "task": "タスク3",
      "result": "タスク3を完了しました",
      "duration": 800,
      "toolCallCount": 1
    }
  ],
  "totalDuration": 1200,
  "averageTaskDuration": 1000
}
```


## 並列処理の違い

### 並列タスク処理 vs 並列サブグラフ実行

| 項目 | 並列タスク処理 (`/parallel-events`) | 並列サブグラフ実行 (`/parallel-subgraphs`) |
|------|-------------------------------------|--------------------------------------------|
| **Koog機能** | EventHandler (onToolCall) | StringSubgraphResult |
| **処理段階** | 1段階（直接実行） | 3段階（分析→処理→最終化） |
| **追跡内容** | ツール呼び出しイベント | 各段階の処理結果 |
| **用途** | イベント監視が必要な場合 | 複雑な処理を段階的に実行 |
| **レスポンス** | イベント詳細とメトリクス | 各段階の結果と集約結果 |

## 学習のポイント

### 何を学べるか

1. **イベント駆動型の開発**
   - エージェントの動作をリアルタイムで追跡
   - ツール呼び出しのタイミングを記録
   - メトリクスの収集と分析

2. **段階的処理（サブグラフ）**
   - 複雑なタスクを小さなステップに分割
   - 各段階の結果を個別に確認可能
   - 処理の流れを可視化

3. **並列処理パターン**
   - 複数タスクの同時実行
   - 処理時間の最適化（最長タスクの時間のみ）
   - 各タスクの独立した結果管理

### 実装の特徴

- **EventHandler**: `onToolCall`と`onAgentFinished`でエージェントのライフサイクルを追跡
- **StringSubgraphResult**: 各処理段階の結果を文字列として保持
- **並列実行**: Kotlinのコルーチンを使用した効率的な並列処理
- **セッション管理**: 各処理にセッションIDを付与

## ファイル構成

```
phase3/
├── README.md                  # このファイル
├── agent/
│   ├── SimpleEventAgent.kt    # シンプルなEventHandler実装
│   ├── KoogEventHandlerAgent.kt # 詳細なイベント管理
│   └── SubgraphAgent.kt       # サブグラフ処理実装
├── config/
│   └── Phase3Config.kt        # 設定クラス
├── controller/
│   └── Phase3Controller.kt    # REST APIコントローラー
└── dto/
    └── Phase3Dto.kt           # データ転送オブジェクト
```

## 動作確認方法

1. **アプリケーション起動**
   ```bash
   task up
   ```

2. **API情報の確認**
   ```bash
   curl http://localhost:8080/api/phase3/info | jq
   ```

3. **シンプルな例から試す**
   ```bash
   # 最も基本的な例
   curl -X POST http://localhost:8080/api/phase3/simple-events \
     -H "Content-Type: application/json" \
     -d '{"message": "テスト"}'
   ```

4. **レスポンスを確認**
   - `success`: 処理が成功したか
   - `message`: AIの応答
   - `events`: 処理中のイベントログ
   - `duration`: 処理時間（ミリ秒）

## 注意事項

- Koog v0.3.0のノードAPIは主に内部実装用であり、ユーザー向けの公開APIとしては限定的です
- 廃止予定のファイル（`@Deprecated`マーク）は使用しないでください

## 参考ドキュメント

- [Koog Agent Events](https://docs.koog.ai/agent-events/)
- [Koog API Reference](https://api.koog.ai/agents/)
- [StringSubgraphResult API](https://api.koog.ai/agents/agents-ext/ai.koog.agents.ext.agent/-string-subgraph-result/)
