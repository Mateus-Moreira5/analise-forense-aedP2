package br.edu.icev.aed.forense;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SolucaoForense implements AnaliseForenseAvancada {
    public SolucaoForense() {
    }

    @Override
    public Set<String> desafio1_encontrarSessoesInvalidas(String caminhoArquivoCsv) throws IOException {
        Set<String> sessoesInvalidas = new HashSet<>();
        Map<String, Stack<String>> pilhaSessoes = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            String linha;
            boolean primeiraLinha = true;

            while ((linha = br.readLine()) != null) {
                if (primeiraLinha) {
                    primeiraLinha = false;
                    continue;
                }
                String[] campos = linha.split(",");
                if (campos.length < 4)
                    continue;

                String userID = campos[1].trim();
                String sessionID = campos[2].trim();
                String actionType = campos[3].trim();
                if (!pilhaSessoes.containsKey(userID)) {
                    pilhaSessoes.put(userID, new Stack<>());
                }
                Stack<String> pilhaUsuario = pilhaSessoes.get(userID);
                if ("LOGIN".equals(actionType)) {
                    if (!pilhaUsuario.isEmpty()) {
                        sessoesInvalidas.add(pilhaUsuario.peek());
                    }
                    pilhaUsuario.push(sessionID);
                } else if ("LOGOUT".equals(actionType)) {
                    if (pilhaUsuario.isEmpty()) {
                        sessoesInvalidas.add(sessionID);
                    } else if (!pilhaUsuario.peek().equals(sessionID)) {
                        sessoesInvalidas.add(pilhaUsuario.peek());
                        sessoesInvalidas.add(sessionID);
                    } else {
                        pilhaUsuario.pop();
                    }
                }
            }
        }
        for (Stack<String> pilha : pilhaSessoes.values()) {
            sessoesInvalidas.addAll(pilha);
        }
        return sessoesInvalidas;
    }

    @Override
    public List<String> desafio2_reconstruirLinhaTempo(String caminhoArquivoCsv, String sessionId) throws IOException {
        Queue<String> acoesNaSessao = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] colunas = linha.split(",", -1);
                if (colunas.length < 7) continue;

                String currentSessionId = colunas[2].trim();
                String actionType = colunas[3].trim();

                if (currentSessionId.equals(sessionId)) {
                    acoesNaSessao.offer(actionType);
                }
            }
        } catch (IOException e) {
            throw new IOException("Erro ao ler o arquivo de logs");
        }
        List<String> linhaDoTempo = new LinkedList<>();
        while (!acoesNaSessao.isEmpty()) {
            linhaDoTempo.add(acoesNaSessao.poll());
        }
        return linhaDoTempo;
    }

    @Override
    public List<Alerta> desafio3_priorizarAlertas(String caminhoArquivoCsv, int tamanhoCiclo) throws IOException {
        PriorityQueue<Alerta> alertaPriority = new PriorityQueue<>();
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            String linha;

            while ((linha = br.readLine()) != null) {
                String[] colunas = linha.split(",", -1);
                if (colunas.length < 6) continue;

                int timestamp = Integer.parseInt(colunas[0].trim());
                String actionType = colunas[3].trim();
                int severityLevel = Integer.parseInt(colunas[5].trim());
                alertaPriority.offer(new Alerta(timestamp, actionType, severityLevel));
            }
        } catch (IOException e) {
            throw new IOException("Erro ao ler o arquivo de logs");
        }
        List<Alerta> topAlertas = new LinkedList<>();
        int contador = 0;
        while (contador < tamanhoCiclo && !alertaPriority.isEmpty()) {
            topAlertas.add(alertaPriority.poll());
            contador++;
        }
        return topAlertas;
    }

    @Override
    public Map<Long, Long> desafio4_encontrarPicosTransferencia(String caminhoArquivo) throws IOException {
        return new HashMap<>();
    }

    @Override
    public Optional<List<String>> desafio5_rastrearContaminacao(String caminhoArquivo, String recursoInicial,
            String recursoAlvo) throws IOException {
        if (recursoInicial.equals(recursoAlvo)) {
            return Optional.of(Arrays.asList(recursoInicial));
        }

        Map<String, List<String>> grafo = new HashMap<>();
        Map<String, List<String>> sessoes = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            br.readLine();

            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",");
                if (campos.length < 5)
                    continue;

                String sessionId = campos[2].trim();
                String recurso = campos[4].trim();

                sessoes.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(recurso);
            }
        }

        // ✅ CORREÇÃO: Ordenar sessões para garantir ordem consistente
        List<String> sessoesOrdenadas = new ArrayList<>(sessoes.keySet());
        Collections.sort(sessoesOrdenadas);

        for (String sessionId : sessoesOrdenadas) {
            List<String> recursos = sessoes.get(sessionId);
            for (int i = 0; i < recursos.size() - 1; i++) {
                String from = recursos.get(i);
                String to = recursos.get(i + 1);

                if (!from.equals(to)) {
                    grafo.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
                }
            }
        }

        Map<String, String> anterior = new HashMap<>();
        Queue<String> fila = new LinkedList<>();
        Set<String> visitado = new HashSet<>();

        fila.add(recursoInicial);
        visitado.add(recursoInicial);
        anterior.put(recursoInicial, null);

        while (!fila.isEmpty()) {
            String atual = fila.poll();

            if (atual.equals(recursoAlvo)) {
                List<String> caminho = new ArrayList<>();
                while (atual != null) {
                    caminho.add(atual);
                    atual = anterior.get(atual);
                }
                Collections.reverse(caminho);
                return Optional.of(caminho);
            }

            List<String> vizinhos = grafo.get(atual);
            if (vizinhos != null) {
                for (String vizinho : vizinhos) {
                    if (!visitado.contains(vizinho)) {
                        visitado.add(vizinho);
                        anterior.put(vizinho, atual);
                        fila.add(vizinho);
                    }
                }
            }
        }

        return Optional.empty();
    }
}
