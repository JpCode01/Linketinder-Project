import { Candidato } from "../models/Candidato";
import { Empresa, IEmpresaJSON } from "../models/Empresa"
import { Vaga, IVagaJSON } from "../models/Vaga";
import { ICandidatoJSON } from "../models/Candidato";
import { candidatos } from "../data/DadosIniciais";


export class EmpresaService {
    cadastrar(empresa: Empresa): void {
        const empresasSalvas = localStorage.getItem("empresas")
        

        // Pega as empresas do LocalStorage e cadastra novamente

        const empresas: Empresa[] =
            empresasSalvas ? JSON.parse(empresasSalvas)
            : []

        empresas.push(empresa)

        localStorage.setItem(
            "empresas",
            JSON.stringify(empresas)
        )
    }

    curtirCandidato(empresa: Empresa, candidato: Candidato): boolean {

        if (empresa == null || candidato == null) {
            throw "Empresa ou candidato não podem ser nulos"
        }

        const empresasSalvas = localStorage.getItem("empresas")

        if (empresasSalvas == null) {
            throw "Nenhuma empresa encontrada"
        }

        const empresasJson: IEmpresaJSON[] = JSON.parse(empresasSalvas)

        const empresaEncontrada = empresasJson.find(
            empresaJson => empresaJson._cnpj === empresa.cnpj
        )

        if (empresaEncontrada == null) {
            throw "Empresa não encontrada"
        }

        const candidatoJaCurtido = empresaEncontrada.candidatosCurtidos.some(
            candidatoCurtido =>
                candidatoCurtido._cpf === candidato.cpf
        )

        if (candidatoJaCurtido) {
            return false
        }

        empresaEncontrada.candidatosCurtidos.push({
            _cpf: candidato.cpf,
            _nome: candidato.nome,
            competencias: candidato.getCompetencias,
            _formacao: candidato.formacao,
            _descricao: candidato.descricao
        })

        localStorage.setItem(
            "empresas",
            JSON.stringify(empresasJson)
        )

        localStorage.setItem(
            "empresaLogada",
            JSON.stringify(empresaEncontrada)
        )

    return true
    }

    adicionarVaga(empresa: Empresa, vaga: Vaga) {
        if (empresa == null || vaga == null) {
            throw "Empresa ou vaga não podem ser nulos"
        }
        const empresasSalvas = localStorage.getItem("empresas")
        if (empresasSalvas == null) {
            throw "Nenhuma empresa cadastrada"
        }

        const empresasJson: IEmpresaJSON[] = JSON.parse(empresasSalvas)

        const empresaEncontrada = empresasJson.find(
            empresaJson => empresaJson._cnpj === empresa.cnpj
        )

        if (empresaEncontrada == null) {
            throw "Empresa não encontrada"
        }

        const vagaJson: IVagaJSON = {
            _nome: vaga.nome,
            _descricao: vaga.descricao,
            _empresa: vaga.empresa,
            _tipo: vaga.tipo,
            _localizacao: vaga.localização,
            competencias: vaga.getCompetencias,
            candidatosQueCurtiram: []
        }

        empresaEncontrada.vagas.push(vagaJson)

        localStorage.setItem(
            "empresas",
            JSON.stringify(empresasJson)
        )

        localStorage.setItem(
            "empresaLogada",
            JSON.stringify(empresaEncontrada)
        )
    }

    buscarEmpresa(nomeProcurado: string): Empresa | undefined {
        const empreasSalvas = localStorage.getItem("empresas")

        if (empreasSalvas == null) {
            return undefined
        }

        const empresaJson: IEmpresaJSON[] = JSON.parse(empreasSalvas)

        const empresaEncontrada = empresaJson.find(
            empresa => empresa._nome === nomeProcurado
        )

        if (empresaEncontrada == null) {
            return undefined
        }

        const empresaConvertida: Empresa = new Empresa(
            empresaEncontrada._cnpj,
            empresaEncontrada._pais,
            empresaEncontrada._nome,
            empresaEncontrada._email,
            empresaEncontrada._estado,
            empresaEncontrada._cep,
            empresaEncontrada._descricao
        )

        const vagasConvertidas: Vaga[] =
            empresaEncontrada.vagas.map(
                    vagaJson => {
                        const vaga = new Vaga(
                            vagaJson._nome,
                            vagaJson._descricao,
                            empresaConvertida.nome,
                            vagaJson._tipo,
                            vagaJson._localizacao
                        )

                        vaga.setCompetencias(vagaJson.competencias)

                        const candidatosConvertidos: Candidato[] =
                            vagaJson.candidatosQueCurtiram.map(
                                candidatoJson => {
                                    const candidato = new Candidato(
                                        candidatoJson._cpf,
                                        0,
                                        candidatoJson._formacao,
                                        candidatoJson._nome,
                                        "",
                                        "",
                                        "",
                                        candidatoJson._descricao
                                    )

                                    candidato.setCompetencias(
                                        candidatoJson.competencias
                                    )

                                    return candidato
                                }
                            )

                vaga.setCandidatosQueCurtiram(candidatosConvertidos)

                return vaga
            }
        )

        const candidatosConvertidos: Candidato[] =
        empresaEncontrada.candidatosCurtidos.map(
            candidatoJson => {
                const candidato = new Candidato(
                    candidatoJson._cpf,
                    0,
                    candidatoJson._formacao,
                    candidatoJson._nome,
                    "",
                    "",
                    "",
                    candidatoJson._descricao
                )

                candidato.setCompetencias(
                    candidatoJson.competencias
                )

                return candidato
            }
        )

        empresaConvertida.setVagas(vagasConvertidas)

        empresaConvertida.setCandidatosCurtidos(
        candidatosConvertidos
        )

        return empresaConvertida
    }

    buscarEmpresaLogada(): Empresa | undefined {
        const empresaSalva = localStorage.getItem("empresaLogada")
        if (empresaSalva == null) {
            return undefined
        }

        const empresaJson: IEmpresaJSON =
        JSON.parse(empresaSalva)


        const empresaConvertida: Empresa = new Empresa(
            empresaJson._cnpj,
            empresaJson._pais,
            empresaJson._nome,
            empresaJson._email,
            empresaJson._estado,
            empresaJson._cep,
            empresaJson._descricao
        )


        const vagasConvertidas: Vaga[] =
        empresaJson.vagas.map(
            vagaJson => {
                const vaga = new Vaga(
                    vagaJson._nome,
                    vagaJson._descricao,
                    empresaConvertida.nome,
                    vagaJson._tipo,
                    vagaJson._localizacao
                )

                vaga.setCompetencias(vagaJson.competencias)

                const candidatosDaVaga: Candidato[] =
                    vagaJson.candidatosQueCurtiram.map(
                        candidatoJson => {
                            const candidato = new Candidato(
                                candidatoJson._cpf,
                                0,
                                candidatoJson._formacao,
                                candidatoJson._nome,
                                "",
                                "",
                                "",
                                candidatoJson._descricao
                            )

                            candidato.setCompetencias(
                                candidatoJson.competencias
                            )

                            return candidato
                        }
                    )

                vaga.setCandidatosQueCurtiram(candidatosDaVaga)

                return vaga
            }
        )

          const candidatosConvertidos: Candidato[] =
        empresaJson.candidatosCurtidos.map(
            candidatoJson => {
                const candidato = new Candidato(
                    candidatoJson._cpf,
                    0,
                    candidatoJson._formacao,
                    candidatoJson._nome,
                    "",
                    "",
                    "",
                    candidatoJson._descricao
                )

                candidato.setCompetencias(
                    candidatoJson.competencias
                )

                return candidato
            }
        )
        


        empresaConvertida.setVagas(vagasConvertidas)

        empresaConvertida.setCandidatosCurtidos(candidatosConvertidos)

        return empresaConvertida
    }
}