import { Empresa } from "../models/Empresa";
import { EmpresaService} from "../services/EmpresaService"

const empresaService: EmpresaService = new EmpresaService

export function exibirPageEmpresa(): void {
        const empresa = empresaService.buscarEmpresaLogada()
        if (empresa != null) {
            const apelidoAvatar = empresa.nome.split(" ")

            document.getElementById("profile-avatar")!.innerHTML =
            apelidoAvatar[0].charAt(0) + apelidoAvatar[1].charAt(0)  
            document.getElementById("user-avatar")!.innerHTML =
            apelidoAvatar[0].charAt(0) + apelidoAvatar[1].charAt(0) 
            document.getElementById("profile-name")!.innerHTML =
            empresa.nome
            document.getElementById("user-name")!.innerHTML =
            empresa.nome
            document.getElementById("profile-description")!.innerHTML =
            empresa.descricao
        }
}