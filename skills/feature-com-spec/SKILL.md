---
name: feature-com-spec
description: Processo SDD genérico para desenvolvimento orientado por especificação
---

## Objetivo
Guiar o desenvolvimento incremental com especificação aprovada antes de qualquer código.

## Quando utilizar
Quando o usuário solicitar uma funcionalidade que exija análise, documentação e implementação controlada.

## Entradas
- AGENTS.md (contexto)
- Caminho da especificação configurado por funcionalidade (exemplo: docs/specs/<feature>/spec.md)
- Caminho do plano configurado por funcionalidade (exemplo: docs/specs/<feature>/plano.md)
Definir os caminhos conforme as convenções do repositório ou a indicação do usuário; as referências a spec.md e plano.md abaixo usam esses caminhos.

## Pré-requisitos
- Repositório explorado: ler AGENTS.md e o código relevante, incluindo produtores e consumidores afetados
- Requisitos de negócio esclarecidos antes de redigir a especificação: fazer perguntas direcionadas, identificar decisões pendentes e aguardar as respostas do usuário; não substituir respostas por suposições
- Nenhum arquivo alterado antes da aprovação

## Passo a passo
1. Explorar contexto (AGENTS.md) e código relevante do repositório
2. Confirmar APIs reais (construtores, métodos, dependências) antes de implementar
3. Criar/revisar AGENTS.md
4. Esclarecer requisitos de negócio com perguntas direcionadas; registrar decisões pendentes e aguardar as respostas antes de redigir spec.md
5. Redigir spec.md e obter aprovação explícita do usuário antes de planejar ou implementar; resolver questões que impeçam essa aprovação
6. Criar plano (plano.md) e obter sua aprovação
7. Realizar checkpoint obrigatório de commit da especificação aprovada antes da implementação: solicitar autorização explícita para o commit e aguardar se não houver autorização; somente prosseguir após o commit concluído
8. Implementar uma tarefa por vez
9. Validar com testes reais (não afirmar que passaram sem evidência)
10. Registrar consumo de tokens quando disponível

## Regras
- Confirmar APIs reais antes de implementar; não presumir construtores ou métodos
- Não planejar nem escrever código antes da aprovação explícita da spec pelo usuário
- Não implementar antes do commit autorizado da especificação aprovada
- Uma tarefa por vez
- Um commit por tarefa, sempre com autorização explícita do usuário para executar commits
- Não alterar código fora do escopo
- Não inventar estruturas
- Validar com testes; distinguir análise estática, compilação, execução e validação final
- Interromper e consultar o usuário em ambiguidades de negócio ou lacunas na spec
- Preservar arquitetura e padrões existentes
- Registrar consumo de tokens quando disponível

## Rastreabilidade
Relacionar requisitos (spec.md no caminho configurado) → tarefas (plano.md no caminho configurado) → testes → evidências. Se um requisito não for testável, registrar explicitamente.

## Auditoria de testes
Verificar cobertura, qualidade das asserções, isolamento, APIs reais e divergências entre spec e implementação. Consultar arquivos existentes; não inventar métodos.

## Evidências de validação
Registrar comandos executados, resultados e falhas. Se o usuário executar manualmente, aguardar os resultados; não afirmar aprovação antes de recebê-los.

## Limites de autonomia
Alterações fora do escopo, arquitetura ou commits exigem autorização. A aprovação da especificação não autoriza commits; nunca executá-los sem autorização explícita do usuário. Se uma dependência ou mudança adicional for necessária, explicar o motivo e solicitar autorização antes de prosseguir.
Criação de issues e branches pode integrar o fluxo do projeto quando aplicável, mas não é obrigatória para usar esta skill nem exige GitHub.

## Modelos
- spec.md:
  - Contexto e objetivos
  - Escopo e regras de negócio
  - Critérios de aceite
  - Modelo de dados e relacionamentos, quando aplicável
  - Segurança, autorização e visibilidade dos dados, quando aplicável
  - Impacto nas funcionalidades existentes e isolamento das alterações
  - Decisões tomadas e questões pendentes, indicando quais impedem aprovação ou implementação
  - Fora do escopo
- plano.md: tarefas numeradas, objetivo, arquivos, validação, critério de conclusão

## Checklist
- [ ] AGENTS.md revisado e código relevante explorado
- [ ] Caminhos da especificação e do plano definidos por funcionalidade
- [ ] Perguntas direcionadas respondidas pelo usuário antes da redação da spec
- [ ] APIs reais confirmadas
- [ ] spec.md explicitamente aprovada pelo usuário antes do planejamento
- [ ] plano.md aprovado
- [ ] Commit da especificação aprovada explicitamente autorizado e concluído antes da implementação
- [ ] Tarefas implementadas uma a uma
- [ ] Testes validados (evidência registrada)
- [ ] Nenhum arquivo alterado fora do escopo
- [ ] Ambiguidades resolvidas com o usuário