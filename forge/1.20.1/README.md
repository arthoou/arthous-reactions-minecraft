<div align="center">

<img src="src/main/resources/icon.png" alt="Logo de Arthou’s Reactions · Reaction Wheel" width="120">

<h1>Arthou’s Reactions · Reaction Wheel</h1>
<p>Expresse reações e emotes com uma roda acessível dentro do jogo.</p>
<p><strong>Minecraft 1.20.1 · Forge · Java 17</strong></p>

</div>

[Instalação](#instalação) · [Uso](#uso) · [Como funciona](#como-funciona) · [Créditos](#créditos)

## Instalação

Coloque o JAR `build/reobfJar/output.jar` na pasta `mods/` do cliente e do servidor. A saída final do ForgeGradle fica em `build/reobfJar/`. Para distribuir o mod, use essa saída com um nome descritivo como `arthous-reactions-forge-1201-1.1.jar`.

| Minecraft | Loader | Java | Versão do mod |
| --- | --- | --- | --- |
| 1.20.1 | Forge 47.4.10 | 17 | 1.1 |

## Uso

Pressione **Z** para abrir a roda. Use as setas da tela, o scroll ou as teclas direcionais para mudar de página. Clique com o botão esquerdo ou confirme com **Enter** ou **Espaço**. **Esc** fecha a roda.

## Como funciona

A roda reúne **36 reações** com ícones e expressões, como risada, piscada, coração e surpresa. Ao selecionar uma, o cliente envia a escolha por um canal Forge. O servidor retransmite a reação aos clientes da região, que desenham o efeito sobre o jogador.

No multiplayer, use a mesma variante **1.1** no cliente e no servidor. O atalho pode ser alterado nos controles.

## Créditos

Implementação da roda e versão Forge por [Arthou](https://github.com/arthoou).

## Compilar

```bash
java -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain build
```

A saída de distribuição aparece no caminho indicado em **Instalação**.
