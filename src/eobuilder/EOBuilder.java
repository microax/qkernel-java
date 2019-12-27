package com.qkernel.eobuilder;
/**
 * Generates EO, VO, and the Container files
 *
 * History:
 * --------
 * 06/06/19 Michael Gill  Refactored to use EOBuilderConfig 
 *                        and moved into qkernel proper
 *
 * 01/18/06 Nathan Oertel Initial Creation 
 */
public class EOBuilder
{
    /**
     * This function generates the Container, EO, and VO.
     *
     * @param args - arguments passed to the program
     */
    public static void main(String args[])
    {
	EOBuilderConfig config  = new EOBuilderConfig(args);
	String driver           = config.getJdbcDriver();
	String dsn              = config.getConnStr();
	String username         = config.getConnUsername();
	String password         = config.getConnPassword();
	String containerPackage = config.getContainerPackageName();
	String containerClass   = config.getContainerClassName();
	String containerDir     = config.getContainerDir();
	String modelPackage     = config.getModelPackageName();
	String modelDir         = config.getModelDir();	
	try
        {
            /** build the container */
            ContainerBuilder cb=new ContainerBuilder(driver,dsn, username, password);
            cb.packageName=containerPackage;
            cb.importName=modelPackage;
            cb.containerName=containerClass;
            cb.outputDir=containerDir;
            cb.doBuild();

            /** build the entity objects */
            EntityObjectBuilder eob=new EntityObjectBuilder(driver,dsn, username, password);
            eob.packageName=modelPackage;
            eob.useMorphers=false;
            eob.outputDir=modelDir;
	    eob.config=config;
            eob.doBuild();

            /** build the value objects */
            ValueObjectBuilder vob=new ValueObjectBuilder(driver,dsn, username, password);
            vob.packageName=modelPackage;
            vob.createMorphers=false;
            vob.outputDir=modelDir;
            vob.doBuild();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}

