import { Candidato } from "../models/Candidato";
import { CandidatoService } from "../services/CandidatoService"
import { VagaService } from "../services/VagaService"

const candidatoService: CandidatoService = new CandidatoService

export function exibirPageCandidato(): void {
    const candidato = candidatoService.buscarCandidatoLogado()
    if (candidato != null) {

        const apelidoAvatar = candidato.nome.split(" ")

        document.getElementById("profile-avatar")!.innerHTML =
            apelidoAvatar[0].charAt(0) + apelidoAvatar[1].charAt(0)   
        document.getElementById("user-avatar")!.innerHTML =
            apelidoAvatar[0].charAt(0) + apelidoAvatar[1].charAt(0)   
        document.getElementById("profile-name")!.innerHTML =
        candidato.nome
        document.getElementById("user-name")!.innerHTML =
        candidato.nome
        document.getElementById("profile-description")!.innerHTML =
        candidato.descricao
    }
}