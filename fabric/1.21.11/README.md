<div align="center">

<img src="src/main/resources/icon.png" alt="Logo de Arthou’s Reactions" width="120">

<h1>Arthou’s Reactions</h1>
<p>Expresse reações e emotes com uma roda acessível dentro do jogo.</p>
<p><strong>Minecraft 1.21.11 · Fabric · Java 21</strong></p>

</div>

[Instalação](#instalação) · [Uso](#uso) · [Como funciona](#como-funciona) · [Créditos](#créditos)

## Instalação

Coloque o JAR `build/libs/arthous-reactions-fabric-12111-2.0.0.jar` na pasta `mods/` do cliente e do servidor. Instale também **Fabric API** para esta versão do Minecraft.

| Minecraft | Loader | Java | Versão do mod |
| --- | --- | --- | --- |
| 1.21.11 | Fabric 0.19.2 | 21 | 2.0.0 |

## Uso

Pressione **Z** para abrir a roda. Use as setas da tela, o scroll ou as teclas direcionais para mudar de página. Clique com o botão esquerdo ou confirme com **Enter** ou **Espaço**. **Esc** fecha a roda. Na reação **Pizza Hi**, o botão direito escolhe a variação com o som de despedida.

## Como funciona

- **Roda de reações:** reúne reações como alegria e irritação e gestos Pizza de cumprimento, palmas, risada, medo, coração e outros. A página muda sem sair da roda.
- **Exibição:** cada reação usa os efeitos e sons definidos pelo catálogo. Com o canal do servidor disponível, a seleção é compartilhada com clientes compatíveis. Sem o canal, o jogador vê a reação local.
- **Servidor Fabric:** instale este JAR em `mods/` com Fabric API. Os jogadores que exibem as reações usam o mod no cliente.
- **Servidor Paper:** coloque `arthous-reactions-paper-12111-2.0.0.jar` em `plugins/` e mantenha o JAR Fabric nos clientes. A ponte repassa as reações no mesmo mundo. Para gerar o plugin, rode `./gradlew :paper-plugin:build` e veja `paper-plugin/build/libs/`.

## Créditos

Desenvolvimento do mod e da ponte Paper por [Arthou](https://github.com/arthoou).

## Compilar

```bash
java -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain build
```

A saída de distribuição aparece no caminho indicado em **Instalação**.
