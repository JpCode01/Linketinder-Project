
import {
    Chart,
    BarController,
    BarElement,
    CategoryScale,
    LinearScale
} from "chart.js"

Chart.register(
    BarController,
    BarElement,
    CategoryScale,
    LinearScale
)

export function criarGraficoCompetencias(
    contagem: Map<string, number>
): void {

    const canvas = document.getElementById(
        "competencia-chart"
    ) as HTMLCanvasElement | null

    if (canvas == null) {
        return
    }
    
    Chart.getChart(canvas)?.destroy()

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