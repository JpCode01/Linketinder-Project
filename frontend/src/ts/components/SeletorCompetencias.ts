export class SeletorCompetencias {
    private readonly catalogo: string[]
    private readonly selecionadas = new Set<string>()
    private readonly pesquisa = document.createElement("input")
    private readonly sugestoes = document.createElement("div")
    private readonly selecao = document.createElement("div")
    private readonly status = document.createElement("p")

    constructor(container: HTMLElement, competencias: string[]) {
        this.catalogo = Array.from(new Set(competencias))
        container.innerHTML = ""
        container.classList.add("seletor-competencias")

        const label = document.createElement("label")
        label.textContent = "Pesquisar competências"
        this.pesquisa.type = "search"
        this.pesquisa.autocomplete = "off"
        this.pesquisa.placeholder = "Digite parte do nome e selecione uma sugestão"
        this.pesquisa.oninput = (): void => this.renderizarSugestoes()
        this.pesquisa.onkeydown = (evento: KeyboardEvent): void => {
            // Enter no texto não seleciona nem submete o formulário.
            if (evento.key === "Enter") evento.preventDefault()
        }
        label.appendChild(this.pesquisa)
        this.sugestoes.className = "competencias-sugestoes"
        this.sugestoes.setAttribute("role", "group")
        this.sugestoes.setAttribute("aria-label", "Sugestões de competências")
        this.selecao.className = "competencias-selecionadas"
        this.selecao.setAttribute("role", "group")
        this.selecao.setAttribute("aria-label", "Competências selecionadas")
        this.status.setAttribute("role", "status")
        container.appendChild(label)
        container.appendChild(this.status)
        container.appendChild(this.sugestoes)
        container.appendChild(this.selecao)
        this.renderizarSugestoes()
    }

    obterSelecionadas(): string[] {
        return Array.from(this.selecionadas)
    }

    limpar(): void {
        this.selecionadas.clear()
        this.pesquisa.value = ""
        this.renderizarSelecao()
        this.renderizarSugestoes()
    }

    private renderizarSugestoes(): void {
        this.sugestoes.innerHTML = ""
        const termo = this.pesquisa.value.trim().toLocaleLowerCase("pt-BR")
        const nomes = this.catalogo.filter(nome =>
            !this.selecionadas.has(nome) && nome.toLocaleLowerCase("pt-BR").includes(termo)
        )
        this.status.textContent = this.catalogo.length === 0
            ? "Nenhuma competência disponível."
            : nomes.length === 0
                ? "Nenhuma competência encontrada para selecionar."
                : `${nomes.length} sugestão(ões). Selecione para adicionar; use Tab e Enter pelo teclado.`

        for (const nome of nomes) {
            const botao = document.createElement("button")
            botao.type = "button"
            botao.textContent = nome
            botao.onclick = (): void => {
                this.selecionadas.add(nome)
                this.pesquisa.value = ""
                this.renderizarSelecao()
                this.renderizarSugestoes()
                this.pesquisa.focus()
            }
            this.sugestoes.appendChild(botao)
        }
    }

    private renderizarSelecao(): void {
        this.selecao.innerHTML = ""
        for (const nome of this.selecionadas) {
            const botao = document.createElement("button")
            botao.type = "button"
            botao.textContent = `${nome} ×`
            botao.setAttribute("aria-label", `Remover ${nome}`)
            botao.onclick = (): void => {
                this.selecionadas.delete(nome)
                this.renderizarSelecao()
                this.renderizarSugestoes()
                this.pesquisa.focus()
            }
            this.selecao.appendChild(botao)
        }
    }
}
