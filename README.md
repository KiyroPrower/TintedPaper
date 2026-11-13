# TintedPaper

**TintedPaper é um fork AI-driven do Paper, desenvolvido como projeto pessoal para um servidor privado de Minecraft.**

As otimizações, correções e configurações são orientadas pelas necessidades desse servidor, com foco em desempenho, mecanismos do jogo e estabilidade. O projeto está em desenvolvimento e suas alterações passam por análise, testes de regressão e validação no ambiente de destino.

## Versão e construção

O alvo atual é Minecraft **26.3**, com **Java 25**.

```powershell
.\gradlew.bat applyAllPatches
.\gradlew.bat :tintedpaper-server:test createTintedPaperJar
```

Em Linux ou macOS, use `./gradlew` no lugar de `.\gradlew.bat`. Os módulos são `tintedpaper-api` e `tintedpaper-server`. O JAR executável é gerado em `build/dist/`, com o empacotador Paperclip padrão.

As builds do CI seguem o versionamento do Paper: `26.3.build.N-alpha`. Compilações locais sem `BUILD_NUMBER` usam o identificador de desenvolvimento `26.3.local-SNAPSHOT`, também usado pelo Paper.

Cada lote de alterações deve entrar por uma branch e um pull request. O CI reaplica os patches, executa os testes e verifica os artefatos. Após a integração em `ver/26.3`, publica uma prerelease numerada com JAR e SHA-256. O canal atual é ALPHA e a migração ainda está em andamento.

[Releases](https://github.com/KiyroPrower/TintedPaper/releases) · [Construções e artefatos](https://github.com/KiyroPrower/TintedPaper/actions/workflows/build-263.yml) · [Guia de contribuição](CONTRIBUTING.md)

## Autoria e licenças

**Todos os patches continuam sendo de direito autoral de seus respectivos criadores.** Sua inclusão no TintedPaper não transfere a autoria nem a titularidade dessas contribuições. Os créditos, avisos de copyright e termos de licença originais permanecem aplicáveis.

O projeto incorpora contribuições do Paper e de outras implementações, identificadas nos cabeçalhos dos patches. Consulte [LICENSE.md](LICENSE.md), o [diretório de licenças](licenses) e os avisos de cada patch para os termos correspondentes.
