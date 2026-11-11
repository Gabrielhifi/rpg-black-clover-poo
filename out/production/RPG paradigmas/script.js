const campoNome = document.getElementById('campo-nome');
const btnSaudar = document.getElementById('btn-saudar'); 
const saudacao = document.getElementById('saudacao');

btnSaudar.addEventListener('click', async function() {
    const nome = campoNome.value;
    await delay(2000); // Aguarda 2 segundos
    saudacao.textContent = `Olá, ${nome}!`;
});
