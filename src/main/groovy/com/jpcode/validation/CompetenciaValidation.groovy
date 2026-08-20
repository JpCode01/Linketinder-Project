package com.jpcode.validation

import com.jpcode.enums.CompetenciasEnum

class CompetenciaValidation {
    boolean validarCompetencia(String competenciaString, List competenciaList) {
        try {
            CompetenciasEnum competencia = CompetenciasEnum.valueOf(competenciaString)

            if (competenciaList.contains(competencia)) {
                return false
            }

            return true

        } catch (IllegalArgumentException e) {
            return false
        }
    }
}
