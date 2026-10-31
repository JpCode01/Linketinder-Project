import { CandidatoService } from "../services/CandidatoService";
import { CurtidaCandidatoService } from "../services/CurtidaCandidatoService";
import { CurtidaVagaService } from "../services/CurtidaVagaService";
import { EmpresaService } from "../services/EmpresaService";
import { RelatorioCompetenciasService } from "../services/RelatorioCompetenciasService";
import { VagaService } from "../services/VagaService";

import { CandidatoApiClient } from "../api/clientes/CandidatoApiClient";
import { CompetenciaApiClient } from "../api/clientes/CompetenciaApiClient";
import { CurtidaCandidatoApiClient } from "../api/clientes/CurtidaCandidatoApiClient";
import { CurtidaVagaApiClient } from "../api/clientes/CurtidaVagaApiClient";
import { EmpresaApiClient } from "../api/clientes/EmpresaApiClient";
import { MatchApiClient } from "../api/clientes/MatchApiClient";
import { ReferenciaApiClient } from "../api/clientes/ReferenciaApiClient";
import { VagaApiClient } from "../api/clientes/VagaApiClient";
import { CompetenciaService } from "../services/CompetenciaService";
import { MatchService } from "../services/MatchService";
import { ReferenciaService } from "../services/ReferenciaService";

export class ServiceConfig {

    criarCandidatoService(): CandidatoService {
        return new CandidatoService(
            new CandidatoApiClient()
        )
    }

    criarRelatorioCompetenciasService(): RelatorioCompetenciasService {
        return new RelatorioCompetenciasService(
            this.criarVagaService(),
            this.criarCurtidaVagaService()
        )
    }

    criarEmpresaService(): EmpresaService {
    return new EmpresaService(
        new EmpresaApiClient()
    )
}

    criarVagaService(): VagaService {
        return new VagaService(
            new VagaApiClient()
        )
    }

    criarCurtidaVagaService(): CurtidaVagaService {
        return new CurtidaVagaService(
            new CurtidaVagaApiClient()
        )
    }

    criarCurtidaCandidatoService(): CurtidaCandidatoService {
        return new CurtidaCandidatoService(
            new CurtidaCandidatoApiClient()
        )
    }

    criarMatchService(): MatchService {
        return new MatchService(
            new MatchApiClient()
        )
    }

    criarCompetenciaService(): CompetenciaService {
        return new CompetenciaService(
            new CompetenciaApiClient()
        )
    }

    criarReferenciaService(): ReferenciaService {
        return new ReferenciaService(
            new ReferenciaApiClient()
        )
    }
}