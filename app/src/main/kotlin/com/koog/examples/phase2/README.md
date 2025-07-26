# Phase 2-01: カスタムツール作成（基本編）

## 概要

このサンプルは、Koog 0.3.0でカスタムツールを作成し、エージェントに統合する方法を示します。
実際の外部API（OpenWeatherMap、News API）と連携する実践的な例を通じて、ツール開発の基礎を学びます。

## 実装されているツール

### 1. WeatherTools
- `getWeather`: 指定された都市の現在の天気情報を取得
- `getWeatherWithAdvice`: 天気情報を取得し、アドバイスも提供

### 2. NewsTools
- `searchNews`: キーワードに基づいてニュースを検索
- `getTopHeadlines`: 最新のヘッドラインニュースを取得（国・カテゴリー別）

### 3. TextAnalysisTools
- `analyzeText`: テキストの基本的な統計情報を分析
- `extractPatterns`: テキスト内のURL、メールアドレス、ハッシュタグなどを抽出
- `analyzeCharacterTypes`: 文字種別（ひらがな、カタカナ、漢字など）を分析
- `analyzeUrl`: URLからWebページのコンテンツを取得して分析 **NEW!**

### 4. ビルトインツール
- `askUser`: ユーザーに質問をする
- `sayToUser`: ユーザーに情報を伝える

## セットアップ

### 1. 環境変数の設定

`.env`ファイルに以下のAPIキーを設定してください：

```bash
# Google AI API Key (必須)
GOOGLE_API_KEY=your_google_api_key_here

# OpenWeatherMap API Key
OPENWEATHER_API_KEY=your_openweather_api_key_here

# News API Key
NEWS_API_KEY=your_news_api_key_here
```

APIキーの取得方法：
- Google AI: https://aistudio.google.com/apikey
- OpenWeatherMap: https://openweathermap.org/api
- News API: https://newsapi.org/

### 2. アプリケーションの起動

```bash
# Dockerを使用する場合
task up

# または直接実行
./gradlew bootRun
```

## API エンドポイント

### チャット機能
```bash
POST /api/phase2/tools/chat
Content-Type: application/json

{
  "message": "東京の天気を教えて"
}
```

### ツール情報の取得
```bash
GET /api/phase2/tools/info
```

## 使用例

### 天気情報の取得
```bash
curl -X POST http://localhost:8080/api/phase2/tools/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "東京の現在の天気を教えてください"}'
```

### ニュース検索
```bash
curl -X POST http://localhost:8080/api/phase2/tools/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "AIに関する最新ニュースを3件教えて"}'
```

### テキスト分析
```bash
curl -X POST http://localhost:8080/api/phase2/tools/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "次のテキストを分析して: Koog 0.3.0がリリースされました！詳細はhttps://example.comをご覧ください。#Koog #AI"}'
```

### URL分析（NEW!）
```bash
curl -X POST http://localhost:8080/api/phase2/tools/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "次のURLを分析して: https://zenn.dev/kinakokyoryu/articles/16b80acb2bf525"}'
```

### 複合的な質問
```bash
curl -X POST http://localhost:8080/api/phase2/tools/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "大阪の天気を教えて、天気に関連するニュースがあれば見せて"}'
```

## プロジェクト構造

```
phase2/
├── agent/
│   └── ToolAgent.kt         # ツールを統合したエージェント
├── config/
│   ├── Phase2Config.kt      # エージェント設定
│   └── ApiConfig.kt         # API接続設定
├── controller/
│   └── ToolController.kt    # REST APIエンドポイント
├── dto/
│   ├── ToolRequest.kt       # リクエストDTO
│   ├── ToolResponse.kt      # レスポンスDTO
│   ├── ToolsInfoResponse.kt # ツール情報レスポンスDTO
│   └── api/                 # 外部API用DTO
│       ├── WeatherApiResponse.kt
│       └── NewsApiResponse.kt
├── service/
│   └── HttpClientService.kt # HTTP通信サービス
├── tools/
│   ├── WeatherTools.kt      # 天気情報ツール群
│   ├── NewsTools.kt         # ニュース検索ツール群
│   └── TextAnalysisTools.kt # テキスト分析ツール群（URL分析機能付き）
└── README.md               # このファイル
```

## 技術的なポイント

### 1. ツールの実装（Koog 0.3.0方式）
- `ToolSet`インターフェースを実装してツールクラスを作成
- `@Tool`アノテーションでメソッドをツールとしてマーク
- `@LLMDescription`でツールやパラメータの説明を提供
- Kotlinの型安全性を活用した実装

### 2. ツールレジストリ
- `ToolRegistry`でツールを管理
- `asTools()`拡張関数でToolSetをレジストリに追加
- ビルトインツール（AskUser、SayToUser）も利用可能

### 3. 非同期処理
- Kotlin Coroutinesを使用した非同期API呼び出し
- `suspend`関数による効率的な並行処理
- ツールメソッドも`suspend`関数として定義可能

### 4. エラーハンドリング
- API呼び出しの失敗に対する適切なエラー処理
- ユーザーフレンドリーなエラーメッセージの返却

### 5. Spring Boot統合
- `@Component`によるDI対応
- 設定の外部化とバリデーション
- Google AI Executorの使用（`simpleGoogleAIExecutor`）

## 拡張のアイデア

1. **キャッシュ機能の追加**
   - 同じクエリに対するAPI呼び出しの削減
   - Redisなどを使用した分散キャッシュ

2. **レート制限の実装**
   - API利用制限への対応
   - ユーザーごとの利用制限

3. **新しいツールの追加**
   - 翻訳ツール（DeepL API）
   - 画像生成ツール（DALL-E API）
   - データベース検索ツール

4. **ツールの組み合わせ**
   - 複数ツールの結果を統合した高度な応答
   - ツール間の依存関係の管理

## トラブルシューティング

### APIキーが設定されていない場合
```
IllegalStateException: OpenWeather API key is not configured
```
→ `.env`ファイルにAPIキーを設定してください

### API呼び出しが失敗する場合
- APIキーが正しいか確認
- ネットワーク接続を確認
- APIの利用制限に達していないか確認

### 日本語が文字化けする場合
- リクエストのContent-Typeが`application/json; charset=UTF-8`になっているか確認
- レスポンスの文字コードを確認

## 次のステップ

Phase2-02では、より高度な外部API連携として以下を学習します：
- GraphQL APIとの連携
- 認証が必要なAPIの扱い
- Webhookの実装
- リアルタイムデータの処理
