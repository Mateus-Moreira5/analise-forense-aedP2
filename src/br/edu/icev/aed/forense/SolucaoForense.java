package br.edu.icev.aed.forense;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SolucaoForense implements AnaliseForenseAvancada {
    
    public SolucaoForense() {}
    
    // ========== DESAFIO 1: Sessões Inválidas (JÁ OTIMIZADO) ==========
    @Override
    public Set<String> desafio1_encontrarSessoesInvalidas(String caminhoArquivoCsv) throws IOException {
        Set<String> sessoesInvalidas = new HashSet<>();
        Map<String, Stack<String>> pilhasUsuarios = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            String linha = br.readLine(); // Pula cabeçalho
            
            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",");
                if (campos.length < 4) continue;
                
                String userId = campos[1].trim();
                String sessionId = campos[2].trim();
                String actionType = campos[3].trim();
                
                if (!pilhasUsuarios.containsKey(userId)) {
                    pilhasUsuarios.put(userId, new Stack<>());
                }
                
                Stack<String> pilha = pilhasUsuarios.get(userId);
                
                if ("LOGIN".equals(actionType)) {
                    if (!pilha.isEmpty()) {
                        sessoesInvalidas.add(pilha.peek());
                    }
                    pilha.push(sessionId);
                } else if ("LOGOUT".equals(actionType)) {
                    if (pilha.isEmpty() || !pilha.peek().equals(sessionId)) {
                        sessoesInvalidas.add(sessionId);
                    } else {
                        pilha.pop();
                    }
                }
            }
        }
        
        // Sessões não finalizadas
        for (Stack<String> pilha : pilhasUsuarios.values()) {
            sessoesInvalidas.addAll(pilha);
        }
        
        return sessoesInvalidas;
    }
    
    // ========== DESAFIO 2: Reconstruir Timeline (JÁ OTIMIZADO) ==========
    @Override
    public List<String> desafio2_reconstruirLinhaTempo(String caminhoArquivoCsv, String sessionId) throws IOException {
        List<String> timeline = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            String linha = br.readLine(); // Pula cabeçalho
            
            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",", -1);
                if (campos.length < 4) continue;
                
                String currentSessionId = campos[2].trim();
                String actionType = campos[3].trim();
                
                if (currentSessionId.equals(sessionId)) {
                    timeline.add(actionType);
                }
            }
        }
        
        return timeline;
    }
    
    // ========== DESAFIO 3: Priorizar Alertas (VERSÃO SUPER OTIMIZADA) ==========
    @Override
    public List<Alerta> desafio3_priorizarAlertas(String caminhoArquivoCsv, int n) throws IOException {
        // 🚀 OTIMIZAÇÃO: Min-Heap com capacidade fixa para O(N log K) em vez de O(N log N)
        if (n <= 0) return new ArrayList<>();
        
        PriorityQueue<Alerta> minHeap = new PriorityQueue<>(n + 1, 
            Comparator.comparingInt(Alerta::getSeverityLevel)
        );
        
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            String linha = br.readLine(); // Pula cabeçalho
            
            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",", -1);
                if (campos.length < 7) continue;
                
                try {
                    long timestamp = Long.parseLong(campos[0].trim());
                    String userId = campos[1].trim();
                    String sessionId = campos[2].trim();
                    String actionType = campos[3].trim();
                    String targetResource = campos[4].trim();
                    int severityLevel = Integer.parseInt(campos[5].trim());
                    long bytesTransferred = Long.parseLong(campos[6].trim());
                    
                    Alerta alerta = new Alerta(
                        timestamp, userId, sessionId, actionType, 
                        targetResource, severityLevel, bytesTransferred
                    );
                    
                    minHeap.offer(alerta);
                    
                    // 🚀 Mantém apenas os N maiores elementos
                    if (minHeap.size() > n) {
                        minHeap.poll(); // Remove o menor
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        
        // 🚀 Converte para lista ordenada (maior para menor)
        List<Alerta> resultado = new ArrayList<>(minHeap);
        resultado.sort((a1, a2) -> 
            Integer.compare(a2.getSeverityLevel(), a1.getSeverityLevel())
        );
        
        return resultado;
    }
    
    // ========== DESAFIO 4: Picos de Transferência (JÁ OTIMIZADO) ==========
    @Override
    public Map<Long, Long> desafio4_encontrarPicosTransferencia(String caminhoArquivo) throws IOException {
        Map<Long, Long> resultado = new HashMap<>();
        List<long[]> eventos = new ArrayList<>(); // 🚀 Usando array primitivo para melhor performance
        
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha = br.readLine(); // Pula cabeçalho
            
            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",");
                if (campos.length < 7) continue;
                
                try {
                    long timestamp = Long.parseLong(campos[0].trim());
                    long bytes = Long.parseLong(campos[6].trim());
                    
                    if (bytes > 0) {
                        eventos.add(new long[]{timestamp, bytes});
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        
        // 🚀 Ordenação mais eficiente com arrays primitivos
        eventos.sort(Comparator.comparingLong(a -> a[0]));
        
        // Pilha monotônica otimizada
        Stack<Integer> pilha = new Stack<>();
        
        for (int i = eventos.size() - 1; i >= 0; i--) {
            long bytesAtual = eventos.get(i)[1];
            
            while (!pilha.isEmpty() && eventos.get(pilha.peek())[1] <= bytesAtual) {
                pilha.pop();
            }
            
            if (!pilha.isEmpty()) {
                resultado.put(eventos.get(i)[0], eventos.get(pilha.peek())[0]);
            }
            
            pilha.push(i);
        }
        
        return resultado;
    }
    
    // ========== DESAFIO 5: Rastrear Contaminação (OTIMIZADO) ==========
    @Override
    public Optional<List<String>> desafio5_rastrearContaminacao(String caminhoArquivo, 
                                                               String recursoInicial, 
                                                               String recursoAlvo) throws IOException {
        // 🚀 Caso especial otimizado
        if (recursoInicial.equals(recursoAlvo)) {
            return Optional.of(Collections.singletonList(recursoInicial));
        }
        
        // 🚀 Constrói grafo de forma mais eficiente (streaming)
        Map<String, Set<String>> grafo = new HashMap<>();
        String currentSession = null;
        String lastResource = null;
        
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha = br.readLine(); // Pula cabeçalho
            
            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",");
                if (campos.length < 5) continue;
                
                String sessionId = campos[2].trim();
                String recurso = campos[4].trim();
                
                // 🚀 Constrói grafo em tempo real sem armazenar todas as sessões
                if (currentSession == null || !currentSession.equals(sessionId)) {
                    currentSession = sessionId;
                    lastResource = null;
                }
                
                if (lastResource != null && !lastResource.equals(recurso)) {
                    grafo.computeIfAbsent(lastResource, k -> new HashSet<>()).add(recurso);
                }
                
                lastResource = recurso;
            }
        }
        
        // 🚀 BFS otimizado com inicialização mais eficiente
        return encontrarCaminhoBFS(grafo, recursoInicial, recursoAlvo);
    }
    
    // 🚀 Método auxiliar otimizado para BFS
    private Optional<List<String>> encontrarCaminhoBFS(Map<String, Set<String>> grafo, 
                                                      String inicio, String alvo) {
        if (!grafo.containsKey(inicio) && !inicio.equals(alvo)) {
            return Optional.empty();
        }
        
        Map<String, String> predecessor = new HashMap<>();
        Queue<String> fila = new LinkedList<>();
        Set<String> visitados = new HashSet<>();
        
        fila.offer(inicio);
        visitados.add(inicio);
        predecessor.put(inicio, null);
        
        while (!fila.isEmpty()) {
            String atual = fila.poll();
            
            if (atual.equals(alvo)) {
                return Optional.of(reconstruirCaminho(predecessor, alvo));
            }
            
            Set<String> vizinhos = grafo.get(atual);
            if (vizinhos != null) {
                for (String vizinho : vizinhos) {
                    if (visitados.add(vizinho)) { // 🚀 add() retorna true se não estava presente
                        predecessor.put(vizinho, atual);
                        fila.offer(vizinho);
                    }
                }
            }
        }
        
        return Optional.empty();
    }
    
    // 🚀 Reconstrução de caminho otimizada
    private List<String> reconstruirCaminho(Map<String, String> predecessor, String alvo) {
        LinkedList<String> caminho = new LinkedList<>();
        String no = alvo;
        
        while (no != null) {
            caminho.addFirst(no); // 🚀 Mais eficiente que reverse()
            no = predecessor.get(no);
        }
        
        return new ArrayList<>(caminho);
    }
}