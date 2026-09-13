import {
    Chart,
    BarController,
    BarElement,
    CategoryScale,
    LinearScale
} from "chart.js"

import { Empresa } from "../models/Empresa"

Chart.register(
    BarController,
    BarElement,
    CategoryScale,
    LinearScale
)


function contarCompetencias(
    empresa: Empresa
): Map<string, number> {

    const competencias = new Map<string, number>()

    for (const vaga of empresa.getVagas) {

        for (const candidato of vaga.getCandidatosQueCurtiram) {

            for (const competencia of candidato.getCompetencias) {

                const quantidade =
                    competencias.get(competencia) ?? 0

                competencias.set(
                    competencia,
                    quantidade + 1
                )
            }
        }
    }

    return competencias
}


export function criarGraficoCompetencias(
    empresa: Empresa
): void {

    const canvas = document.getElementById(
        "competencia-chart"
    ) as HTMLCanvasElement

    if (canvas == null) {
        return
    }

    const contagem = contarCompetencias(empresa)

    const labels = Array.from(contagem.keys())

    const valores = Array.from(contagem.values())

    new Chart(canvas, {

        type: "bar",

        data: {

            labels: labels,

            datasets: [
                {
                    label: "Candidatos",
                    data: valores
                }
            ]
        },

        options: {

            responsive: true,

            scales: {

                y: {
                    beginAtZero: true,

                    ticks: {
                        stepSize: 1
                    }
                }
            }
        }
    })
}