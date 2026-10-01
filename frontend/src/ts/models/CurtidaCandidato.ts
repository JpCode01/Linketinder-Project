export class CurtidaCandidato {

    constructor(
        private _idEmpresa: number,
        private _idCandidato: number
    ) {}

    get idEmpresa(): number {
        return this._idEmpresa
    }

    get idCandidato(): number {
        return this._idCandidato
    }
}