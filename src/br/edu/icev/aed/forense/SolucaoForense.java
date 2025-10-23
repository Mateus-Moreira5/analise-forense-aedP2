package br.edu.icev.aed.forense;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Collections;

public class SolucaoForense implements AnaliseForenseAvancada {
    public SolucaoForense(){}
    @Override
    public Set<String> encontrarSessoesInvalidas(String caminhoArquivoCsv) throws IOException {
        Set<String> sessoesInvalidas = new HashSet<>();
        Map<String, Stack<String>> pilhaSessoes = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new FileReader(caminhoArquivoCsv))){
            String linha; 
            boolean primeiraLinha = true;
            
            while ((linha = br.readLine()) != null) {
                if(primeiraLinha){
                    primeiraLinha = false;
                    continue;
                }
                String[] campos = linha.split(",");
                if(campos.length < 4) continue;

                String userID = campos[1].trim();
                String sessionID = campos[2].trim();
                String actionType = campos[3].trim();
                if (!pilhaSessoes.containsKey(userID)){
                    pilhaSessoes.put(userID, new Stack<>());
                }
                Stack<String> pilhaUsuario = pilhaSessoes.get(userID);
                if("LOGIN".equals(actionType)){
                    if(!pilhaUsuario.isEmpty()){
                        sessoesInvalidas.add(pilhaUsuario.peek());
                    }
                    pilhaUsuario.push(sessionID);
                } else if("LOGOUT".equals(actionType)){
                    if(pilhaUsuario.isEmpty()){
                        sessoesInvalidas.add(sessionID);
                    } else if (!pilhaUsuario.peek().equals(sessionID)){
                        sessoesInvalidas.add(pilhaUsuario.peek());
                        sessoesInvalidas.add(sessionID);
                    } else {
                        pilhaUsuario.pop();
                    }
                }
            }
        }
        for(Stack<String> pilha : pilhaSessoes.values()){
            sessoesInvalidas.addAll(pilha);
        }
        return sessoesInvalidas;
    }
    @Override
    public List<String> reconstruirLinhaDoTempo(String caminhoArquivoCsv, String sessionId) throws IOException {
        Queue<String> acoesNaSessao = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            String linha;

            if ((linha = br.readLine()) == null) {
                return Collections.emptyList();
            }
            while ((linha = br.readLine()) != null) {
                String[] colunas = linha.split(",");

                if (colunas.length < 7) {
                    continue;
                }

                String currentSessionId = colunas[2].trim();
                String actionType = colunas[3].trim();

                if (currentSessionId.equals(sessionId)) {
                    acoesNaSessao.offer(actionType);
                }
            }
        } catch (IOException e) {

            throw new IOException("Erro ao ler o arquivo de logs: " + e.getMessage(), e);
        }
        List<String> linhaDoTempo = new LinkedList<>();
        while (!acoesNaSessao.isEmpty()) {
            linhaDoTempo.add(acoesNaSessao.poll());
        }
        return linhaDoTempo;
    }
}
