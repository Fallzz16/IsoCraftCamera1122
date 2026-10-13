# IsoCraft Camera — Minecraft Forge 1.12.2

Mod client-side de câmera isométrica com recursos inspirados em **Project Zomboid**.

**Versão:** v0.3.0 (experimental) · **Minecraft:** 1.12.2 · **Forge para compilação:** 14.23.5.2847 · **Java:** 8.

## Recursos

- Câmera isométrica alternável com **F8**.
- Zoom com **F6 / F7**, rotação com **J / L** e projeção com **K** (sem numpad).
- Segurar **X** e arrastar o mouse para olhar em outras direções.
- Cursor livre ativável por **V**, com orientação visual do Steve para o cursor.
- Folhas semitransparentes próximas ao jogador, ativáveis por **H**.
- Mitigações experimentais para zoom oscilante e chunks desaparecendo.

## Compilação automática (GitHub Actions)

O código-fonte está em `src/`; os testes matemáticos independentes estão em `tests/`.
O workflow `.github/workflows/forge1122-build.yml` executa testes e compila usando
**Temurin JDK 8**, **Gradle 4.9** e as dependências reais do **Forge 1.12.2**.

**Primeira compilação aprovada:** [GitHub Actions — execução #2](https://github.com/Fallzz16/IsoCraftCamera1122/actions/runs/38324966271).

Para baixar: abra essa execução, vá à seção **Artifacts** e escolha
`IsoCraft-Camera-Forge1122-v0.3.0`. Extraia o ZIP do artefato para obter
`isocraft-camera-forge1122-0.3.0.jar`.

Para futuras compilações, alterações nos arquivos Java, nos testes ou na
configuração Gradle iniciam o workflow automaticamente. Você também pode
executá-lo manualmente pela aba **Actions**.

## Instalação

1. Use **Minecraft 1.12.2 com Forge** (não Fabric e não Forge 1.21.x).
2. Coloque somente o `.jar` compilado na pasta `mods` do perfil Minecraft 1.12.2.
3. Inicie o jogo e confirme se o IsoCraft Camera aparece em **Mods**.
4. Teste preferencialmente em mundo Criativo novo, sem outros mods.

## Status e limitações

A execução #2 passou pela compilação com o Forge real, pela verificação das
classes e do `mcmod.info`, e publicou o artefato `.jar`.
**Isso não é um teste de jogabilidade:** o renderizador de folhas, o cursor,
a rotação livre e os chunks ainda precisam de validação dentro do jogo.
Esta continua sendo uma versão **experimental**.

Projeto independente do VeilChapter1122.
