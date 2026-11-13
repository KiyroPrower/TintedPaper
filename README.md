# TintedPaper

**TintedPaper é um fork AI-driven do Paper, desenvolvido como projeto pessoal para um servidor privado de Minecraft.**

As otimizações, correções e configurações são orientadas pelas necessidades desse servidor, com foco em desempenho, mecanismos do jogo e estabilidade. O projeto está em desenvolvimento e suas alterações passam por análise, testes de regressão e validação no ambiente de destino.

## Versão e construção

O alvo atual é Minecraft **26.3**, com **Java 25**.

```powershell
.\gradlew.bat applyAllPatches
.\gradlew.bat :leaf-server:test createLeafclipJar
```

Em Linux ou macOS, use `./gradlew` no lugar de `.\gradlew.bat`. Os nomes internos dos módulos e tarefas herdados são mantidos para compatibilidade com o sistema de patches. Os JARs são gerados em `leaf-server/build/libs/`.

[Construções e artefatos](https://github.com/KiyroPrower/TintedPaper/actions/workflows/build-263.yml) · [Guia de contribuição](CONTRIBUTING.md)

## Autoria e licenças

**Todos os patches continuam sendo de direito autoral de seus respectivos criadores.** Sua inclusão no TintedPaper não transfere a autoria nem a titularidade dessas contribuições. Os créditos, avisos de copyright e termos de licença originais permanecem aplicáveis.

O projeto incorpora contribuições do Paper e de outras implementações, identificadas nos cabeçalhos dos patches. Consulte [LICENSE.md](LICENSE.md), o [diretório de licenças](licenses) e os avisos de cada patch para os termos correspondentes.
