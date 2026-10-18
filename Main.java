import java.text.MessageFormat;
import java.util.HashMap;
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
        messages.put("damage_received", "{0} recebeu {1} de dano.");
        messages.put("defeated", ">>> {0} foi derrotado! <<<");
        messages.put("status", "[{0}] Vida: {1}");
        messages.put("anti_magic_strike", "{0} usa Corte Divisor de Demônios em {1}!");
        messages.put("wind_magic", "{0} lança Magia de Vento: Tormenta em {1}!");
        messages.put("no_mana", "{0} não tem mana suficiente!");
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
// 2. CAMADA DE DOMÍNIO (DIP, OCP, LSP)
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
    protected int vida;
    protected int ataque;

    public Personagem(String nome, int vida, int ataque) {
        this.nome = nome;
        this.vida = vida;
        this.ataque = ataque;
    }

    public void atacar(Destrutivel alvo, CombatNotifier notifier) {
        notifier.notify("attack", this.nome, alvo.getNome());
        alvo.receberDano(this.ataque, notifier);
    }

    @Override
    public void receberDano(int valor, CombatNotifier notifier) {
        this.vida = Math.max(0, this.vida - valor);
        notifier.notify("damage_received", this.nome, valor);
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
        notifier.notify("status", this.nome, this.vida);
    }
}

// ==========================================
// 3. ESPECIALIZAÇÕES (Herança e Interfaces)
// ==========================================

// Análogo ao Guerreiro do documento
class CavaleiroAntiMagia extends Personagem implements ComHabilidadeEspecial {
    public CavaleiroAntiMagia(String nome, int vida, int ataque) {
        super(nome, vida, ataque); //[cite: 5]
    }

    @Override
    public void usarHabilidade(Destrutivel alvo, CombatNotifier notifier) {
        int dano = this.ataque + 15; // Dano extra massivo[cite: 1]
        notifier.notify("anti_magic_strike", this.nome, alvo.getNome());
        alvo.receberDano(dano, notifier);
    }
}

// Análogo ao Mago do documento
class CavaleiroMagico extends Personagem implements ComHabilidadeEspecial {
    private int mana; // Atributo específico do mago[cite: 2]

    public CavaleiroMagico(String nome, int vida, int ataque, int mana) {
        super(nome, vida, ataque);
        this.mana = mana; //[cite: 6]
    }

    @Override
    public void usarHabilidade(Destrutivel alvo, CombatNotifier notifier) {
        if (this.mana >= 10) {
            int dano = this.ataque + 20;
            this.mana -= 10;
            notifier.notify("wind_magic", this.nome, alvo.getNome());
            alvo.receberDano(dano, notifier);
        } else {
            notifier.notify("no_mana", this.nome); //[cite: 6]
        }
    }
    
    @Override
    public void mostrarStatus(CombatNotifier notifier) {
        notifier.printRaw(String.format("[%s] Vida: %d | Mana: %d", this.nome, this.vida, this.mana));
    }
}

// Análogo ao Inimigo comum/chefe
class Vilao extends Personagem {
    public Vilao(String nome, int vida, int ataque) {
        super(nome, vida, ataque); //[cite: 6]
    }
}

// ==========================================
// 4. MOTOR DO JOGO INTERATIVO
// ==========================================
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CombatNotifier notifier = new CombatNotifier(new ConsoleOutputChannel(), new PortugueseMessageProvider());

        notifier.printRaw("=======================================");
        notifier.printRaw("    BEM-VINDO AO RPG: BLACK CLOVER     ");
        notifier.printRaw("=======================================");
        notifier.printRaw("Escolha seu personagem:");
        notifier.printRaw("1. Asta (Cavaleiro Anti-Magia - Foco em Dano Físico Bruto)");
        notifier.printRaw("2. Yuno (Cavaleiro Mágico - Foco em Magia de Vento e Mana)");
        notifier.printRaw("Opção: ");
        
        int escolha = scanner.nextInt();
        Personagem heroi;
        
        if (escolha == 1) {
            heroi = new CavaleiroAntiMagia("Asta", 100, 15);
        } else {
            heroi = new CavaleiroMagico("Yuno", 80, 10, 50);
        }

        Personagem chefe = new Vilao("Vetto (Olho do Sol da Meia-Noite)", 120, 18);

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
            
            notifier.printRaw("Opção: ");
            int acao = scanner.nextInt();
            
            notifier.printRaw(""); // Linha em branco
            
            // Turno do Herói
            if (acao == 1) {
                heroi.atacar(chefe, notifier); //[cite: 5, 22]
            } else if (acao == 2 && heroi instanceof ComHabilidadeEspecial) {
                ((ComHabilidadeEspecial) heroi).usarHabilidade(chefe, notifier); //[cite: 13]
            } else {
                notifier.printRaw("Ação inválida. Você perdeu o turno!");
            }
            
            // Verifica se o chefe morreu antes de contra-atacar
            if (chefe.estaDestruido()) {
                break;
            }
            
            // Turno do Inimigo (Age como um adversário comum enfrentando o herói)[cite: 1]
            notifier.printRaw("\nTurno do Inimigo:");
            chefe.atacar(heroi, notifier); //[cite: 6]
        }

        notifier.printRaw("\n=======================================");
        if (heroi.estaDestruido()) {
            notifier.printRaw("FIM DE JOGO. O Reino Clover caiu.");
        } else {
            notifier.printRaw("VITÓRIA! Você protegeu o Reino Clover superou seus limites!");
        }
        notifier.printRaw("=======================================");
        
        scanner.close();
    }
}