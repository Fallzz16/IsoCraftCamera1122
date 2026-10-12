# IsoCraft Camera — Minecraft Forge 1.12.2

Mod de câmera isométrica com recursos inspirados em Project Zomboid.

**Versão:** 0.3.0 (experimental) · **Minecraft:** 1.12.2 · **Forge:** 14.23.5.2847 (compilação) · **Java:** 8.

## Recursos

- Câmera isométrica com alternância por F8, zoom e rotação, sem exigir numpad.
- X + arrastar o mouse para explorar ângulos.
- Cursor livre, jogador olhando para a posição do cursor e folhas com transparência visual.
- Ajustes para reduzir oscilação do zoom e falhas de descarte de chunks.

## Compilação automática

Veja **Actions → IsoCraft Camera 1.12.2 — Build e QA**. Quando o workflow terminar com sucesso, abra a execução e baixe o artefato `IsoCraft-Camera-Forge1122-v0.3.0`, contendo o `.jar` do mod. Não use em Minecraft 1.21.x.

O workflow usa JDK 8 e Gradle 4.9; executa testes matemáticos, compila com Forge real e valida entradas essenciais do JAR. Uma compilação bem-sucedida **não comprova** ausência de problemas visuais. Testes no jogo ainda são necessários.

## Nota sobre a importação

A importação inicial foi automatizada a partir do pacote-fonte da v0.3.0. Os arquivos originais são extraídos, verificados e enviados ao repositório pelo workflow de inicialização (quando a permissão de escrita do GitHub Actions está disponível). Se a publicação de arquivos falhar, a compilação ainda poderá ocorrer e os logs informarão o motivo.

© Projeto IsoCraft Camera.
