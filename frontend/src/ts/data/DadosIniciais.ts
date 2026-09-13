import { Candidato } from "../models/Candidato"
import { Empresa } from "../models/Empresa"
import { Competencia } from "../models/Competencia"


export const candidatos: Candidato[] = [

    new Candidato(
        "123.456.789-01",
        22,
        "Análise e Desenvolvimento de Sistemas",
        "João Silva",
        "joao@email.com",
        "São Paulo",
        "01000-000",
        "Desenvolvedor Backend"
    ),

    new Candidato(
        "234.567.890-12",
        25,
        "Engenharia de Software",
        "Maria Oliveira",
        "maria@email.com",
        "Minas Gerais",
        "30000-000",
        "Desenvolvedora Full Stack"
    ),

    new Candidato(
        "345.678.901-23",
        20,
        "Ciência da Computação",
        "Carlos Santos",
        "carlos@email.com",
        "Rio de Janeiro",
        "20000-000",
        "Desenvolvedor Java"
    ),

    new Candidato(
        "456.789.012-34",
        27,
        "Sistemas de Informação",
        "Ana Souza",
        "ana@email.com",
        "Paraná",
        "80000-000",
        "Desenvolvedora Frontend"
    ),

    new Candidato(
        "567.890.123-45",
        23,
        "Análise e Desenvolvimento de Sistemas",
        "Lucas Ferreira",
        "lucas@email.com",
        "São Paulo",
        "13000-000",
        "Desenvolvedor Backend"
    )

]


candidatos[0].addCompetencia(Competencia.JAVA)
candidatos[0].addCompetencia(Competencia.SPRING)

candidatos[1].addCompetencia(Competencia.JAVASCRIPT)
candidatos[1].addCompetencia(Competencia.ANGULAR)

candidatos[2].addCompetencia(Competencia.JAVA)
candidatos[2].addCompetencia(Competencia.HTML)

candidatos[3].addCompetencia(Competencia.JAVASCRIPT)
candidatos[3].addCompetencia(Competencia.GROOVY)

candidatos[4].addCompetencia(Competencia.JAVA)
candidatos[4].addCompetencia(Competencia.PYTHON)


export const empresas: Empresa[] = [

    new Empresa(
        "12.345.678/0001-01",
        "Brasil",
        "Tech Solutions",
        "contato@techsolutions.com",
        "São Paulo",
        "01000-000",
        "Empresa de desenvolvimento de software"
    ),

    new Empresa(
        "23.456.789/0001-02",
        "Brasil",
        "CodeLab",
        "contato@codelab.com",
        "Minas Gerais",
        "30000-000",
        "Empresa especializada em tecnologia"
    ),

    new Empresa(
        "34.567.890/0001-03",
        "Brasil",
        "DevHouse",
        "contato@devhouse.com",
        "Rio de Janeiro",
        "20000-000",
        "Empresa de soluções digitais"
    ),

    new Empresa(
        "45.678.901/0001-04",
        "Brasil",
        "Software Corp",
        "contato@softwarecorp.com",
        "Paraná",
        "80000-000",
        "Desenvolvimento de sistemas corporativos"
    ),

    new Empresa(
        "56.789.012/0001-05",
        "Brasil",
        "NextTech",
        "contato@nexttech.com",
        "São Paulo",
        "13000-000",
        "Empresa focada em inovação e tecnologia"
    )

]