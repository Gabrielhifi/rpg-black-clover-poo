import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// ==========================================
// 1. CAMADA DE INTERNACIONALIZAÇÃO E SAÍDA (SRP)
// ==========================================
interface MessageProvider {
    String getMessage(String key, Object... args);
}

class PortugueseMessageProvider implements MessageProvider {
    private final Map<String, String> messages = new HashMap<>();

    public PortugueseMessageProvider() {
        messages.put("attack", "{0} ataca {1}!");
        messages.put("damage_received", "{0} recebeu {1} de dano! (Defesa mitigou {2})");
        messages.put("defeated", ">>> {0} foi derrotado! <<<");
        messages.put("status", "[{0}] Vida: {1}/{2} | Defesa: {3}");
        messages.put("anti_magic_strike", "{0} usa Corte Divisor de Demônios em {1}!");
        messages.put("wind_magic", "{0} lança Magia de Vento: Tormenta em {1}!");
        messages.put("no_mana", "{0} não tem mana suficiente!");
        messages.put("use_item", "{0} usou {1}!");
        messages.put("heal", "{0} recuperou {1} de vida!");
    }

    @Override
    public String getMessage(String key, Object... args) {
        String template = messages.getOrDefault(key, key);
        return MessageFormat.format(template, args);
    }
}

interface OutputChannel {
    void print(String message);
}

class ConsoleOutputChannel implements OutputChannel {
    @Override
    public void print(String message) {
        System.out.println(message);
    }
}

class CombatNotifier {
    private final OutputChannel output;
    private final MessageProvider messages;

    public CombatNotifier(OutputChannel output, MessageProvider messages) {
        this.output = output;
        this.messages = messages;
    }

    public void notify(String key, Object... args) {
        output.print(messages.getMessage(key, args));
    }

    public void printRaw(String text) {
        output.print(text);
    }
}

// ==========================================
// 2. SISTEMA DE ITENS (OCP, SRP)
// ==========================================
interface Item {
    String getNome();
    void usar(Personagem alvo, CombatNotifier notifier);
}

class PocaoCura implements Item {
    private final int poderCura = 40;

    @Override
    public String getNome() {
        return "Poção de Cura";
    }

    @Override
    public void usar(Personagem alvo, CombatNotifier notifier) {
        alvo.curar(poderCura, notifier);
    }
}

// ==========================================
// 3. CAMADA DE DOMÍNIO (DIP, OCP, LSP)
// ==========================================
interface Destrutivel {
    void receberDano(int valor, CombatNotifier notifier);
    boolean estaDestruido();
    String getNome();
}

interface ComHabilidadeEspecial {
    void usarHabilidade(Destrutivel alvo, CombatNotifier notifier);
}

abstract class Personagem implements Destrutivel {
    protected String nome;
    protected int vidaMaxima;
    protected int vida;
    protected int ataque;
    protected int defesa;
    protected List<Item> inventario;

    public Personagem(String nome, int vida, int ataque, int defesa) {
        this.nome = nome;
        this.vidaMaxima = vida;
        this.vida = vida;
        this.ataque = ataque;
        this.defesa = defesa;
        this.inventario = new ArrayList<>();
    }

    public void adicionarItem(Item item) {
        inventario.add(item);
    }

    public void curar(int valor, CombatNotifier notifier) {
        this.vida = Math.min(this.vidaMaxima, this.vida + valor);
        notifier.notify("heal", this.nome, valor);
    }

    public void atacar(Destrutivel alvo, CombatNotifier notifier) {
        notifier.notify("attack", this.nome, alvo.getNome());
        alvo.receberDano(this.ataque, notifier);
    }

    @Override
    public void receberDano(int valor, CombatNotifier notifier) {
        // Cálculo de dano considerando a defesa
        int danoReal = Math.max(0, valor - this.defesa);
        int danoMitigado = valor - danoReal;

        // Correção do erro de sintaxe:
        this.vida = Math.max(0, this.vida - danoReal);

        notifier.notify("damage_received", this.nome, danoReal, danoMitigado);
        if (estaDestruido()) {
            notifier.notify("defeated", this.nome);
        }
    }

    @Override
    public boolean estaDestruido() {
        return this.vida == 0;
    }

    @Override
    public String getNome() {
        return this.nome;
    }

    public void mostrarStatus(CombatNotifier notifier) {
        notifier.notify("status", this.nome, this.vida, this.vidaMaxima, this.defesa);
    }

    public boolean temItens() {
        return !inventario.isEmpty();
    }

    public void mostrarInventario(CombatNotifier notifier) {
        notifier.printRaw("Inventário de " + this.nome + ":");
        for (int i = 0; i < inventario.size(); i++) {
            notifier.printRaw((i + 1) + ". " + inventario.get(i).getNome());
        }
    }

    public void usarItem(int indice, CombatNotifier notifier) {
        if (indice >= 0 && indice < inventario.size()) {
            Item item = inventario.remove(indice);
            notifier.notify("use_item", this.nome, item.getNome());
            item.usar(this, notifier);
        } else {
            notifier.printRaw("Seleção de item inválida!");
        }
    }
}

// ==========================================
// 4. ESPECIALIZAÇÕES (Herança e Interfaces)
// ==========================================

class CavaleiroAntiMagia extends Personagem implements ComHabilidadeEspecial {
    public CavaleiroAntiMagia(String nome, int vida, int ataque, int defesa) {
        super(nome, vida, ataque, defesa);
    }

    @Override
    public void usarHabilidade(Destrutivel alvo, CombatNotifier notifier) {
        int dano = this.ataque + 15; // Dano extra ignorando parte da defesa inimiga
        notifier.notify("anti_magic_strike", this.nome, alvo.getNome());
        alvo.receberDano(dano, notifier);
    }
}

class CavaleiroMagico extends Personagem implements ComHabilidadeEspecial {
    private int mana;

    public CavaleiroMagico(String nome, int vida, int ataque, int defesa, int mana) {
        super(nome, vida, ataque, defesa);
        this.mana = mana;
    }

    @Override
    public void usarHabilidade(Destrutivel alvo, CombatNotifier notifier) {
        if (this.mana >= 10) {
            int dano = this.ataque + 20;
            this.mana -= 10;
            notifier.notify("wind_magic", this.nome, alvo.getNome());
            alvo.receberDano(dano, notifier);
        } else {
            notifier.notify("no_mana", this.nome);
        }
    }

    @Override
    public void mostrarStatus(CombatNotifier notifier) {
        notifier.printRaw(String.format("[%s] Vida: %d/%d | Defesa: %d | Mana: %d",
                this.nome, this.vida, this.vidaMaxima, this.defesa, this.mana));
    }
}

class Vilao extends Personagem {
    public Vilao(String nome, int vida, int ataque, int defesa) {
        super(nome, vida, ataque, defesa);
    }
}

// ==========================================
// 5. MOTOR DO JOGO INTERATIVO
// ==========================================
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CombatNotifier notifier = new CombatNotifier(new ConsoleOutputChannel(), new PortugueseMessageProvider());

        notifier.printRaw("=======================================");
        notifier.printRaw("    BEM-VINDO AO RPG: BLACK CLOVER     ");
        notifier.printRaw("=======================================");
        notifier.printRaw("Escolha seu personagem:");
        notifier.printRaw("1. Asta (Cavaleiro Anti-Magia - Foco em Dano e Defesa Física)");
        notifier.printRaw("2. Yuno (Cavaleiro Mágico - Foco em Magia e Mana)");
        notifier.printRaw("Opção: ");

        int escolha = scanner.nextInt();
        Personagem heroi;

        if (escolha == 1) {
            heroi = new CavaleiroAntiMagia("Asta", 120, 20, 8);
        } else {
            heroi = new CavaleiroMagico("Yuno", 90, 15, 4, 50);
        }

        // Adiciona itens ao jogador
        heroi.adicionarItem(new PocaoCura());
        heroi.adicionarItem(new PocaoCura());

        // Inimigo chefe (Atributos: Vida, Ataque, Defesa)
        Personagem chefe = new Vilao("Vetto (Olho do Sol da Meia-Noite)", 150, 25, 5);

        notifier.printRaw("\nUm poderoso inimigo apareceu!");

        // Loop principal do combate
        while (!heroi.estaDestruido() && !chefe.estaDestruido()) {
            notifier.printRaw("\n--- Turno ---");
            heroi.mostrarStatus(notifier);
            chefe.mostrarStatus(notifier);

            notifier.printRaw("\nSua ação:");
            notifier.printRaw("1. Atacar (Ataque Básico)");

            if (heroi instanceof ComHabilidadeEspecial) {
                notifier.printRaw("2. Usar Magia / Habilidade Especial");
            }
            if (heroi.temItens()) {
                notifier.printRaw("3. Inventário (Usar Item)");
            }

            notifier.printRaw("Opção: ");
            int acao = scanner.nextInt();
            notifier.printRaw("");

            // Turno do Herói
            if (acao == 1) {
                heroi.atacar(chefe, notifier);
            } else if (acao == 2 && heroi instanceof ComHabilidadeEspecial) {
                ((ComHabilidadeEspecial) heroi).usarHabilidade(chefe, notifier);
            } else if (acao == 3 && heroi.temItens()) {
                heroi.mostrarInventario(notifier);
                notifier.printRaw("Escolha o número do item (ou 0 para cancelar): ");
                int escolhaItem = scanner.nextInt();
                if (escolhaItem > 0) {
                    heroi.usarItem(escolhaItem - 1, notifier);
                } else {
                    notifier.printRaw("Ação cancelada. Turno perdido!");
                }
            } else {
                notifier.printRaw("Ação inválida. Você perdeu o turno!");
            }

            if (chefe.estaDestruido()) {
                break;
            }

            // Turno do Inimigo
            notifier.printRaw("\nTurno do Inimigo:");
            chefe.atacar(heroi, notifier);
        }

        notifier.printRaw("\n=======================================");
        if (heroi.estaDestruido()) {
            notifier.printRaw("FIM DE JOGO. O Reino Clover caiu.");
        } else {
            notifier.printRaw("VITÓRIA! Você superou seus limites!");
        }
        notifier.printRaw("=======================================");

        scanner.close();
    }
}