# IsoCraft Camera — Minecraft Forge 1.12.2

Mod experimental de câmera isométrica com recursos inspirados em Project Zomboid.

**Versão:** v0.3.0 · **Minecraft:** 1.12.2 · **Forge para compilação:** 14.23.5.2847 · **Java:** 8.

## Recursos

- Câmera isométrica com F8, zoom e rotação, sem necessidade de teclado numérico.
- X + arrastar o mouse para olhar em outras direções.
- Cursor livre, Steve acompanhando visualmente o cursor e folhas semitransparentes.
- Correções experimentais para oscilação de zoom e desaparecimento de chunks.

## Status da importação do projeto

O workflow de compilação está publicado em `.github/workflows/forge1122-build.yml`. **Os arquivos-fonte ainda precisam ser enviados** à raiz deste repositório para executar o primeiro build.

Como enviar pelo navegador:

1. Baixe o pacote `IsoCraftCamera_1.12.2_v0.3.0_ARQUIVOS_PARA_UPLOAD_GITHUB.zip` fornecido no ChatGPT.
2. Extraia o ZIP no computador.
3. No GitHub, vá a **Add file → Upload files** e arraste **o conteúdo extraído**, incluindo as pastas `src`, `tests` e arquivos `build.gradle`, `gradle.properties`, `settings.gradle`, `TEST-MATH.sh`. Não envie o ZIP como um único arquivo.
4. Confirme **Commit changes** na branch `main`.

## Compilação automática

O envio dos arquivos-fonte inicia o workflow **Actions → IsoCraft Camera 1.12.2 - Build e QA**. Ele instala JDK 8 e Gradle 4.9, executa testes matemáticos, compila com Forge real e confere entradas essenciais do JAR.

Quando o workflow ficar verde, abra a execução e baixe o artefato `IsoCraft-Camera-Forge1122-v0.3.0`. Dentro dele estará `isocraft-camera-forge1122-0.3.0.jar`.

Uma compilação bem-sucedida ainda não comprova ausência de bugs visuais; câmera, cursor, folhagem e chunks precisam de teste dentro do Minecraft.

**Importante:** este repositório é separado do projeto VeilChapter1122.
