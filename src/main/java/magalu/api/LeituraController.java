package magalu.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@RestController
@RequestMapping("/arquivo")
public class LeituraController {

    @Value("${arquivo.caminho}") // Injeta o caminho do arquivo do application.properties
    private String caminhoArquivo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public String lerArquivoComoJson() {
        ArrayNode transacoes = objectMapper.createArrayNode();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String line;
            while ((line = br.readLine()) != null) {
                ObjectNode transacao = processarLinha(line);
                transacoes.add(transacao);
            }
        } catch (IOException e) {
            ObjectNode erro = objectMapper.createObjectNode();
            erro.put("status", "erro");
            erro.put("mensagem", "Erro ao ler o arquivo: " + e.getMessage());
            return erro.toString();
        }

        ObjectNode resposta = objectMapper.createObjectNode();
        resposta.put("status", "sucesso");
        resposta.set("transacoes", transacoes);

        return resposta.toString();
    }

    private ObjectNode processarLinha(String linha) {
        ObjectNode dados = objectMapper.createObjectNode();

        try {
            // Ajuste os índices conforme necessário
            String idTransacao = linha.substring(0, 10).trim();      // ID da transação
            String nomeCliente = linha.substring(10, 45).trim();     // Nome do cliente
            String numeroConta = linha.substring(45, 60).trim();     // Número da conta
            String codigoTransacao = linha.substring(60, 70).trim(); // Código da transação
            String valorTransacao = linha.substring(70, 78).trim();  // Valor da transação
            String dataTransacao = linha.substring(78).trim();       // Data da transação (YYYYMMDD)

            dados.put("id_transacao", idTransacao);
            dados.put("nome_cliente", nomeCliente);
            dados.put("numero_conta", numeroConta);
            dados.put("codigo_transacao", codigoTransacao);
            dados.put("valor_transacao", Double.parseDouble(valorTransacao));
            dados.put("data_transacao", formatarData(dataTransacao));
        } catch (Exception e) {
            dados.put("erro", "Falha ao processar linha: " + linha);
        }

        return dados;
    }

    private String formatarData(String data) {
        if (data.matches("\\d{8}")) { // Verifica se são exatamente 8 dígitos numéricos
            return data.substring(0, 4) + "-" + data.substring(4, 6) + "-" + data.substring(6, 8);
        }
        return "Data inválida";
    }


}
