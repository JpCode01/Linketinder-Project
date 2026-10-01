export class CurtidaVaga {

    constructor(
        private _idCandidato: number,
        private _idVaga: number
    ) {}

    get idCandidato(): number {
        return this._idCandidato
    }

    get idVaga(): number {
        return this._idVaga
    }
}