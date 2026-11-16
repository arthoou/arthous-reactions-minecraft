<div align="center">

<img src="src/main/resources/icon.png" alt="Logo de Arthou’s Reactions" width="120">

<h1>Arthou’s Reactions</h1>
<p>Expresse reações e emotes com uma roda acessível dentro do jogo.</p>
<p><strong>Minecraft 1.21.1 · NeoForge · Java 21</strong></p>

</div>

[Instalação](#instalação) · [Uso](#uso) · [Como funciona](#como-funciona) · [Créditos](#créditos)

## Instalação

Coloque o JAR `build/libs/arthous-reactions-neoforge-1211-2.6.3.jar` na pasta `mods/` do cliente e do servidor. Player Animator e Bendy lib já estão incluídos no JAR.

| Minecraft | Loader | Java | Versão do mod |
| --- | --- | --- | --- |
| 1.21.1 | NeoForge 21.1.228 | 21 | 2.6.3 |

## Uso

Pressione **Z** para abrir a roda. Use as setas da tela, o scroll ou as teclas direcionais para mudar de página. Clique com o botão esquerdo ou confirme com **Enter** ou **Espaço**. **Esc** fecha a roda.

Pressione **P** com a roda aberta para alternar entre a roda centralizada e o modo com prévia do personagem. A preferência e a página ficam em `config/arthousreactions-wheel.properties`. Passar o mouse sobre um emote mostra a prévia com a skin e o equipamento do jogador.

## Como funciona

- **Gestos:** a roda inclui cumprimentos, palmas, choro, risada, medo, 67, Hearts, Anjo e outros. Os gestos disparam as poses e efeitos do catálogo e chegam aos outros clientes compatíveis pelo servidor.
- **Prévia:** exibe a pose em um personagem separado, sem tocar o áudio da reação no mundo. Veja as [maquetes da roda](design/gui-appearance/PROPOSTA.md) e as [prévias dos gestos](design/gesture-polish/PREVIEW.md).
- **Camadas 3D:** a integração opcional com **3D Skin Layers**, versão 1.11.4 ou posterior para o mesmo alvo NeoForge, acompanha as dobras de braços, pernas e roupas durante emotes.
- **Scuba:** aparece na segunda página e dança em loop. Solte Shift e pressione a tecla novamente para interromper. Trocar de reação também encerra a dança.

| Seleção de Scuba | Resultado |
| --- | --- |
| Esquerdo | Dança |
| Direito | Dança e música |
| Shift + esquerdo | Dança com gatinho |
| Shift + direito | Dança, música e gatinho |

## Créditos

Implementação, roda, integração de renderização e adaptação de animações por [Arthou](https://github.com/arthoou). O JAR inclui **Player Animator** e **Bendy lib**, de **KosmX**. O modelo original **Little Angel** é de **eosuki**, e a conversão das asas usa o formato do **Customizable Player Models**. A integração com **3D Skin Layers** usa a API de **tr7zw**. Veja os [avisos e licenças das dependências e do modelo](THIRD_PARTY_NOTICES.md).

## Compilar

```bash
java -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain build
```

O build executa testes de pose e renderização em CPU. Para gerar amostras de animação, veja as [instruções do preview](design/angel-animation/PREVIEW.md). Os scripts de captura precisam do pacote Node **Playwright** e de Chromium.
