package jason.infra.centralised;

public interface RunCentralisedMASMBean {
    public int     getNbAgents();
    public boolean killAg(String agName);
    public void    finish(int deadline, boolean stopJVM, int exitValue);
}
