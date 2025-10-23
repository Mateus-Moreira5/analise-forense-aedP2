package br.edu.icev.aed.forense;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface AnaliseForenseAvancada {
    Set<String> encontrarSessoesInvalidas(String caminhoArquivoCsv) throws IOException;

    List<String> reconstruirLinhaDoTempo(String caminhoArquivoCsv, String sessionId) throws IOException;
}
