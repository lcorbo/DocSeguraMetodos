SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectImpresoras]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[SelectImpresoras] 
	
AS
BEGIN

SELECT  
	ID,
	nombreImpresora
	
FROM         
	TV_LPAImpresoras

END' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[InsertHistorialImpresion]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ====================================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Insterta historial de impresiones
-- ====================================================================
CREATE PROCEDURE [dbo].[InsertHistorialImpresion]
	-- Add the parameters for the stored procedure here
	@usuario varchar(64), 	
	@tipoCopia int, 
	@comentario varchar(256), 
	@codigoDoc varchar(64),
	@version varchar(8), 
	@idDocumento varchar(16),
	@idImpresora int,
	@ID int out 
AS
BEGIN


    -- Insert statements for procedure here	
	INSERT INTO TD_LPAHistorial_Impresion (
		usuario, 
		fecha, 
		tipoCopia, 
		comentario, 
		codigoDoc,
		version, 
		idDocumento,
		idImpresora
	)
	values (
		@usuario, 
		getdate(),
		@tipoCopia, 
		@comentario, 
		@codigoDoc,
		@version, 
		@idDocumento,
		@idImpresora
		
	)

	SET @ID = @@Identity
	PRINT @ID

END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectTipoCopias]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

-- ==================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene los tipos de copia
-- ==================================================
CREATE PROCEDURE [dbo].[SelectTipoCopias] 	
AS
BEGIN

	--Select tipo copias----
	SELECT  
		ID,
		tipoCopia,  
		controlImpresion
		
	FROM         
		TP_LPATipoCopias
	WHERE
		comboDesplegable = ''SI''

END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectHistorialImpresion]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ====================================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene historial de impresiones
-- ====================================================================

CREATE PROCEDURE [dbo].[SelectHistorialImpresion]
	-- Add the parameters for the stored procedure here	 
	@codigoDoc varchar(64)	
	--@idDocumento varchar(16)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here	
	SELECT 
		H.[ID],
		H.[usuario],
		H.[fecha],
		T.[tipoCopia],
		I.[nombreImpresora],
		H.[comentario],
		H.[codigoDoc],
		H.[version],
		H.[idDocumento]
	FROM 
		[TD_LPAHistorial_Impresion] H
	LEFT JOIN 
		[TP_LPATipoCopias] T ON H.tipoCopia = T.ID
	LEFT JOIN 
		[TV_LPAImpresoras] I ON H.idImpresora=I.ID
	WHERE 
		H.[codigoDoc] = @codigoDoc

END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[HistorialImpresion]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'



-- =============================================
-- Author:	PRO - SAR
-- Create date: 13/01/2011
-- Description:	Trae el hitorial de impresion de un documento de InQ
-- =============================================
CREATE PROCEDURE [dbo].[HistorialImpresion] 
@CodDoc		varchar(50),
@version    varchar(2)
AS
BEGIN

declare @lId	int
declare @Version	varchar(8)
declare @NombreUsuario	varchar(32)
declare @FechaImpresion	datetime
declare @TipoImpresion	varchar(20)
declare @Comentarios	varchar(1024)

select pd.lName as NombreUsuario, al.datecompleted as FechaImpresion, sIdActdf as TipoImpresion, al.sComment as Comentarios, dv.version as Version
from inq..documentversion dv
inner join inq..activitylog al on dv.id = al.iddoc and (sidactdf = ''print'' or sidactdf = ''printControlled'')
inner join inq..participantdef pd on al.idusrdf = pd.id 
where 
siddoc like @CodDoc and version = @version


END




' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Sp_InfoDocInQ]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'




-- =============================================
-- Author:	PRO - SAR
-- Create date: 13/01/2011
-- Description:	Trae la informacion de InQ para los documentos a migrar
-- =============================================
CREATE PROCEDURE [dbo].[Sp_InfoDocInQ] 
@CodDoc		varchar(50),
@version    varchar(2)
AS
BEGIN

declare @lId				int
declare @sFechaEdicion	datetime
declare @sFechaRevision datetime
declare @sFechaAprobacion datetime
declare @sFechaPublicacion datetime
declare @sFechaVencimiento datetime
declare @sTitulo	varchar(100)
declare @CodSector	varchar(100)
declare @CodEmpresa	varchar(100)
declare @aux varchar(50)

--select top 1 * from documentversion where siddoc = ''SOP-CC-000039'' order by version desc
-- Paso 1: Se obtiene el id del documento (VER SI ESTAN VIGENTE)
select @lId = id from inq..documentversion where siddoc like @CodDoc and version = @version

-- Paso 2: Obtengo las fecha de revision, aprobacion, publicacion y edicion
--todo:select * from activitylog where iddoc = 494713 order by datecompleted
select @sFechaEdicion = datecompleted from inq..activitylog where iddoc = @lId and sidactdf = ''Edition'' and datecompleted = (select max(datecompleted) from inq..activitylog where iddoc = @lId and sidactdf = ''Edition'') 
select @sFechaRevision = datecompleted  from inq..activitylog where iddoc = @lId and sidactdf = ''review'' and datecompleted = (select max(datecompleted) from inq..activitylog where iddoc = @lId and sidactdf = ''review'') 
select @sFechaAprobacion = datecompleted from inq..activitylog where iddoc = @lId and sidactdf = ''approval'' and datecompleted = (select max(datecompleted) from inq..activitylog where iddoc = @lId and sidactdf = ''approval'') 
select @sFechaPublicacion = datecompleted from inq..activitylog where iddoc = @lId and sidactdf = ''publication'' and datecompleted = (select max(datecompleted) from inq..activitylog where iddoc = @lId and sidactdf = ''publication'') 

-- Paso 3: Titulo y Sector
select @sTitulo = title, @CodSector = sectorCode from inq..isodoccatalog where [doc-id] = @lId

-- Paso 3: Fecha de Vencimiento y Filial
select @aux = validityDate, @CodEmpresa = companyCode from inq..ISODocCatalog where id = @lId

IF @aux IS NOT NULL
	BEGIN
		IF LEN(@aux) = 38 OR LEN(@aux) = 46 
			SET @sFechaVencimiento = convert(datetime, SUBSTRING(@aux,17,10), 21)
	    ELSE
			SET @sFechaVencimiento = NULL
	END

select @sFechaEdicion as FechaEdicion, @sFechaRevision AS FechaRevision, @sFechaAprobacion as FechaAprobacion, @sFechaPublicacion as FechaVigencia,
@sFechaVencimiento as FechaVencimiento, @sTitulo as Titulo, @CodSector as CodSector, @CodEmpresa as CodEmpresa

END





' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectViewerBotones]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ==================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene botones del viewer validado
--				por clase, sector y grupos
-- ==================================================
CREATE PROCEDURE [dbo].[SelectViewerBotones]
	-- Add the parameters for the stored procedure here	 
	@clase varchar(64),
	@selector varchar(64),
	@sGrupos varchar(2000)
	
AS
BEGIN

	-- Declara Variables----------
	DECLARE @sQuery varchar(2000)
	SET  @sQuery = ''''


    -- Select Botones Viewer------
	SET  @sQuery = @sQuery + ''SELECT ID,clase,grupo,botones  FROM TP_LPAViewer_Botones ''
	SET  @sQuery = @sQuery + ''WHERE clase = ''''''+@clase+''''''''+'' and grupo like ''''%''+@selector+''%'''' and '' 
	SET  @sQuery = @sQuery + ''grupo in (''+@sGrupos +'')''

	EXEC sp_sqlexec @sQuery
		 
	PRINT @sQuery
	

END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[DeleteAnexo]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ======================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Elimina un  anexo
-- ======================================================
CREATE PROCEDURE [dbo].[DeleteAnexo] 
	@idRelacion int
	
AS
BEGIN

DELETE 
FROM   
     TD_LPAAnexos
WHERE 
	idrelacion=@idRelacion

END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[InsertHistorialImpresionMigracion]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[InsertHistorialImpresionMigracion]
       -- Add the parameters for the stored procedure here
       @usuario varchar(64),
       @tipoCopia int,
       @comentario varchar(256),
       @codigoDoc varchar(64),
       @version varchar(8),
       @idDocumento varchar(16),
       @fecha datetime
AS
BEGIN
       -- SET NOCOUNT ON added to prevent extra result sets from
       -- interfering with SELECT statements.
       SET NOCOUNT ON;

   -- Insert statements for procedure here
       INSERT INTO TD_LPAHistorial_Impresion (
               usuario,
               fecha,
               tipoCopia,
               comentario,
               codigoDoc,
               version,
               idDocumento,
               idImpresora
       )
       values (
               @usuario,
               @fecha,
               @tipoCopia,
               @comentario,
               @codigoDoc,
               @version,
               @idDocumento,
               0
       )

END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectViewerBotonesPorGrupo]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ======================================================
-- Author:		Sergio Araki
-- Create date: ---
-- Description: Obtiene los botondes del viewer validado
--				por grupos.
-- ======================================================
CREATE PROCEDURE [dbo].[SelectViewerBotonesPorGrupo]
	-- Add the parameters for the stored procedure here	 
	@sGrupos varchar(2000)
	
AS
BEGIN

	DECLARE @sQuery varchar(2000)
	SET  @sQuery = ''''

    -- Insert statements for procedure here	
	SET  @sQuery = @sQuery + ''SELECT botones  FROM TP_LPAViewer_Botones_Grupo ''
	SET  @sQuery = @sQuery + ''WHERE grupo in (''+@sGrupos +'')''

	EXEC sp_sqlexec @sQuery
		 
	PRINT @sQuery
	

END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectImpresorasPorGrupo]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- =============================================
-- Author:		Jergio Araqui
-- Create date: ---
-- Description:	Obtiene impresoras por grupo.
-- =============================================

CREATE PROCEDURE [dbo].[SelectImpresorasPorGrupo] 
	@sGrupos varchar(2000)
AS
BEGIN

	--Declere variables -------------
	DECLARE @sQuery varchar(2000)
	SET  @sQuery = ''''
    
	--Select impresoras--------------
	SET  @sQuery = @sQuery + ''SELECT ID,nombreImpresora FROM TV_LPAImpresoras ''
	SET  @sQuery = @sQuery + ''WHERE grupo in (''+@sGrupos +'')''

	EXEC sp_sqlexec @sQuery
		 
	PRINT @sQuery

END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[DeleteRelacion]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ======================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Elimina una relacion 
-- ======================================================
CREATE PROCEDURE [dbo].[DeleteRelacion] 
	@idRelacion int
	
AS
BEGIN

DELETE 
FROM   
     TD_LPARelaciones
WHERE 
	idrelacion=@idRelacion

END


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[IncrementaNumeroNomenclador]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[IncrementaNumeroNomenclador]
	@clase varchar(64), 
	@parametros varchar(1024)
AS
BEGIN

declare @vsql Nvarchar(2048)

set @vsql = ''update TD_LPANomenclador set numerador = numerador + 1 where clase = '''''' + @clase + '''''''' + ISNULL(@parametros, '''')

exec sp_sqlexec @vsql

END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[InsertWorkflowComments]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[InsertWorkflowComments]
	@nombreWorkflow varchar(128), 
	@idDocumento varchar(24),
	@idComentario varchar(24),
	@codDocumento varchar(36),
	@version varchar(8)
AS
BEGIN

insert into TD_LPAWorkflow_Comments (nombreWorkflow,idDocumento,codDocumento,version,idComentario) values (@nombreWorkflow,@idDocumento,@codDocumento,@version,@idComentario)

END



' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectRelaciones]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'



-- =============================================================
-- Author:		Juan Pulido
-- ALTER date:	16/08/2011
-- Description: Obtiene relaciones de un documento 
-- ==============================================================
CREATE PROCEDURE [dbo].[SelectRelaciones] 
	@codigoDocOrigen varchar(64),
	@versionDocOrigen varchar(8) = null
AS

BEGIN
	IF (@versionDocOrigen is null) 
		BEGIN
				SELECT     
					idrelacion, 
					codigoDocOrigen,
					versionDocOrigen,
					idOrigen, 
					idDestino, 
					codigoDocDestino,
					versionDocDestino,
					usuario, 
					fechaRelacion
					
				FROM         
					v_relaciones_estados
				WHERE	
					 ((codigoDocOrigen = @codigoDocOrigen) OR
					 (codigoDocDestino = @codigoDocOrigen)) 
					 
		END
	ELSE
		BEGIN	
			SELECT     
				idrelacion, 
				codigoDocOrigen,
				versionDocOrigen,
				idOrigen, 
				idDestino, 
				codigoDocDestino,
				versionDocDestino,
				usuario, 
				fechaRelacion
				
			FROM         
				v_relaciones_estados
			WHERE	
				 ((codigoDocOrigen = @codigoDocOrigen and versionDocOrigen = @versionDocOrigen) OR
				 (codigoDocDestino = @codigoDocOrigen and versionDocDestino = @versionDocOrigen))  
			     

		END
END







' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[BloqueoWord]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[BloqueoWord] 
AS
BEGIN
update TV_LPABotones set Descripcion = (select Descripcion from TV_LPABotones where ID = 1) where ID = 1
END' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectNumeroNomenclador]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[SelectNumeroNomenclador]
	@clase varchar(64), 
	@parametros varchar(1024)
AS
BEGIN

declare @vsql Nvarchar(2048)

set @vsql = ''select numerador from TD_LPANomenclador where clase = '''''' + @clase + '''''''' + ISNULL(@parametros, '''')

exec sp_sqlexec @vsql

END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectReglasNomenclador]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[SelectReglasNomenclador] 
	@clase varchar(64)
AS
BEGIN
	select numerador, propiedades, tipoPropiedades, propiedadCodigo, propiedadNumerador from TP_LPANomenclador where clase = @clase
END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[UpdateImpresion]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[UpdateImpresion]
	@id int
AS
BEGIN
update TD_LPAServiceImpresion set procesada = ''SI'', fechaImpresion = getDate() where ID = @id and procesada = ''NO''
END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[UpdateImpresionConError]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[UpdateImpresionConError]
	@id int,
	@mensaje varchar(256)
AS
BEGIN
update TD_LPAServiceImpresion set procesada = ''SI'', mensajeError = @mensaje, fechaImpresion = getDate() where ID = @id and procesada = ''NO''
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectGruposSinControl]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ====================================================
-- Author:		Juan Pulido
-- Create date: 05/07/2010 - 09:43
-- Description:	Obtiene cantidad registros por 
--------------- por grupos al que pertence un usuario
-- =====================================================
CREATE PROCEDURE [dbo].[SelectGruposSinControl] 
	@sGrupos varchar(2000)
AS
BEGIN

	--Declere variables -------------
	DECLARE @sQuery varchar(2000)
	SET  @sQuery = ''''
    
	--Select impresoras--------------
	SET  @sQuery = @sQuery + ''SELECT count(ID) as cantidad FROM TP_LPAGrupos_SinControl ''
	SET  @sQuery = @sQuery + ''WHERE grupo in (''+@sGrupos +'')''

	EXEC sp_sqlexec @sQuery
		 
	PRINT @sQuery

END


-- ====================================================
-- Author:		Juan Pulido
-- Create date: 05/07/2010 - 09:43
-- Description:	Grupos sin control de impresion
-- =====================================================
CREATE TABLE [TP_LPAGrupos_SinControl](
	ID int IDENTITY(1,1) PRIMARY KEY NOT NULL,
	grupo Varchar(64) NOT NULL,
)' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectDescargas]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ====================================================================
-- Author:		Juan Pulido
-- Create date: 17.10.2011
-- Description: Obtiene descargas permitidas segun clase y extension
-- ====================================================================
CREATE PROCEDURE [dbo].[SelectDescargas] 
    @clase varchar(64) =''at'' ,
	@dosExtension varchar(5) =''cdr''
AS
BEGIN

	--Si descargas>0 se permite la descarga
	SELECT 
		count(ID) as descargas 
	FROM 
		TP_LPADescargas
	WHERE 
		clase = @clase and 
		dosExtension = @dosExtension

END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectInfoWorkflowMail]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[SelectInfoWorkflowMail] 
	@paso varchar(64)
AS
BEGIN

select subject, body, destinatarios, propiedadBPM from TP_LPAWorkflow_Mail where nombrePaso = @paso

END



' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectCantidadCopias]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ====================================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene Cantidad de Copias 
-- ====================================================================
CREATE PROCEDURE [dbo].[SelectCantidadCopias] 
    @codigo varchar(64) ,
	@grupo varchar(64),
    @version varchar(8),
	@retorno int out 
     
    
AS
BEGIN
    

    DECLARE @cantCopias int

	SELECT @cantCopias = cantCopias  
	FROM 
		TD_LPAControl_Impresion 
	WHERE 
		codigo=@codigo and 
		grupo =@grupo and 
		version = @version 


	IF @cantCopias=0 BEGIN        
        SET @retorno = 0
        PRINT ''PUEDES REALIZAR UNA COPIA CONTROLADA '' + CONVERT(VARCHAR(2),@retorno) 
        
	END 
	ELSE IF @cantCopias=1 BEGIN        
        SET @retorno = 1
        PRINT ''SE DEBE EJECUTAR EL PROCESO INSERTAR ORDEN '' + CONVERT(VARCHAR(2),@retorno) 
        
	END 
 
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[InsertControlImpresion]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

-- ====================================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Inserta Control  de impresion con validaciones
-- ====================================================================
CREATE PROCEDURE [dbo].[InsertControlImpresion] 
    @codigo varchar(64) ,
    @version varchar(8),
    @grupo varchar(64),
    @tipoCopia int,
    @retorno int out 
     
    
AS
BEGIN
    
    --Declaracion de variables locales
    DECLARE @query1 int    
    DECLARE @query3 int
    DECLARE @query4 int
    DECLARE @query5 int
    DECLARE @sInsert varchar(2000)
    DECLARE @sUpdate varchar(2000)

    --Inicializa variables        
    SELECT @query1 = count(ID)  FROM TD_LPAControl_Impresion WHERE codigo = @codigo and tipoCopia=@tipoCopia
    SELECT @query3 = count(ID)  FROM TD_LPAControl_Impresion WHERE codigo = @codigo and grupo = @grupo and version = @version and tipoCopia=@tipoCopia        
    SELECT @query4 = count(ID)  FROM TD_LPAControl_Impresion WHERE codigo = @codigo and grupo = @grupo and permitir = ''NO'' and tipoCopia=@tipoCopia
    SELECT @query5 = cantCopias FROM TD_LPAControl_Impresion WHERE codigo = @codigo and grupo = @grupo and version = @version and tipoCopia=@tipoCopia        

    --Query 2
    SET @sInsert  = ''''
    SET @sInsert = @sInsert + ''INSERT INTO TD_LPAControl_Impresion (codigo,version,grupo,cantCopias,tipoCopia,permitir) ''
    SET @sInsert = @sInsert + ''VALUES(''''''+@codigo+'''''', ''''''+@version+'''''', ''''''+@grupo+'''''',1,''+LTRIM(RTRIM(CAST(@tipoCopia AS VARCHAR(2))))+'',''''SI'''')''
    
    --Query 6
    SET @sUpdate  = ''''
    SET @sUpdate = @sUpdate + ''update TD_LPAControl_Impresion set cantCopias = cantCopias+1 where codigo = '''''' +@codigo+ ''''''''
    SET @sUpdate = @sUpdate + ''and grupo = ''''''+@grupo+'''''' and version = ''''''+@version+'''''' and tipoCopia = ''+LTRIM(RTRIM(CAST(@tipoCopia AS VARCHAR(2))))

    --Validaciones    
    IF @query1=0 BEGIN
        SET @sInsert = @sInsert
        EXEC sp_sqlexec @sInsert
        SET @retorno = 1
        PRINT ''QUERY 1 ES 0 '' + CONVERT(VARCHAR(2),@retorno) 
        
    END 
    ELSE IF @query1>0    BEGIN        
        IF @query3>0 BEGIN            
            IF @query4 >0 BEGIN            
                SET @retorno = -1    
                --RAISERROR(''No Se permite la impresión'', 16, 1)
                --RETURN
            END                         
            ELSE IF @query4=0 BEGIN
                    IF @query5=0 BEGIN
                        SET @sUpdate = @sUpdate
                        EXEC sp_sqlexec @sUpdate
                        SET @retorno = 1
                        PRINT ''QUERY 5 ES 0 ''+CONVERT(VARCHAR(2),@retorno) 
                    END 
                    ELSE IF @query5>0 BEGIN
                        SET @retorno = -1
                        --RAISERROR(''No Se permite la impresión'', 16, 1)
                        --RETURN
                    END
            END 
        END    
        ELSE IF @query3=0 BEGIN 
			IF @query4 >0 BEGIN            
                SET @retorno = -1    
                --RAISERROR(''No Se permite la impresión'', 16, 1)
                --RETURN
            END                         
            ELSE BEGIN              
				SET @sInsert = @sInsert
				EXEC sp_sqlexec @sInsert
				SET @retorno = 1
				PRINT ''QUERY 5 ES 0 ''+CONVERT(VARCHAR(2),@retorno)
			END 
        END
    END    
    
    SET    @retorno=@retorno
    PRINT @retorno
 
END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[DeshabilitaImpresion]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[DeshabilitaImpresion]
	@codigo varchar(64)
AS
BEGIN
update TD_LPAControl_Impresion set permitir = ''NO'' where codigo = @codigo
END' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[HabilitaImpresion]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[HabilitaImpresion]
	@codigo varchar(64),
	@grupo varchar(64)
AS
BEGIN
update TD_LPAControl_Impresion set permitir = ''SI'' where codigo = @codigo and grupo = @grupo
END' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[RenuevaImpresion]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[RenuevaImpresion]
	@codigo varchar(64),
	@grupo varchar(64),
	@version varchar(8)
AS
BEGIN
update TD_LPAControl_Impresion set cantCopias = 0 where codigo = @codigo and grupo = @grupo and version = @version
END' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[InsertServiceImpresion]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ====================================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Inserta Servicio de impresion
-- ====================================================================
CREATE PROCEDURE [dbo].[InsertServiceImpresion] 
	-- Add the parameters for the stored procedure here
	@idDocumento varchar(16), 
	@idImpresion int,	
	@usuario varchar(64), 
	@impresora varchar(128), 
	@tipoCopia int
	
AS
BEGIN

    -- Insert statements for procedure here	
	INSERT INTO TD_LPAServiceImpresion (
		idDocumento,
		idImpresion ,		
		usuario, 
		impresora, 
		tipoCopia, 
		procesada
	) 
	VALUES(
		@idDocumento, 
		@idImpresion,		
		@usuario, 
		@impresora, 
		@tipoCopia,
		''NO''
	)

END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectImpresiones]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[SelectImpresiones]
AS
BEGIN

select a.ID, a.idDocumento, a.usuario, c.nombreImpresora, b.tipoCopia, b.leyendaWord
from TD_LPAServiceImpresion a, TP_LPATipoCopias b, TV_LPAImpresoras c
where a.procesada = ''NO''
and a.impresora = c.ID
and a.tipoCopia = b.ID

END' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectViewerBuscadorAnexos]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ==================================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene info clase documental para tab doc adjuntos
-- ==================================================================
CREATE PROCEDURE [dbo].[SelectViewerBuscadorAnexos] 
	@clase 	varchar(256)
AS
BEGIN
	SELECT 
		ID,
		clase,
		descripcion, 
		propiedades,
		nombrePropiedades,
		tipoPropiedades ,
		resultados,
		nombreResultados,
		tipoResultados
		
	FROM 
		TP_LPAViewer_BuscadorAnexos  
	WHERE 
		clase=@clase
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectAllViewerBuscadorAnexos]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ===============================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene clases documentales para tab doc adjuntos
-- ===============================================================
CREATE PROCEDURE [dbo].[SelectAllViewerBuscadorAnexos] 	
AS
BEGIN
	SELECT 
		ID,
		clase,
		descripcion, 
		propiedades,
		nombrePropiedades,
		tipoPropiedades ,
		resultados,
		nombreResultados,
		tipoResultados	
	FROM 
		TP_LPAViewer_BuscadorAnexos 
END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectWorkflowTasks]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

CREATE PROCEDURE [dbo].[SelectWorkflowTasks]
	@nombreWorkflow varchar(128)
AS
BEGIN
select nombrePaso, propiedadUsuarios, documento from TP_LPAWorkflow_Tasks where nombreWorkflow = @nombreWorkflow
END



' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectOrders]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[SelectOrders]
AS
BEGIN
select idOrden, nombreWorkflow, idDocumento, valoresPropiedadesBPM from TD_LPAOrdenes where precargado = ''NO'' and ejecutado = ''NO''
END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[DeleteOrden]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[DeleteOrden]
	@id varchar(24)
	
AS
BEGIN

delete from TD_LPAOrdenes 
where idDocumento = @id and precargado = ''SI'' and ejecutado = ''NO''

END' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[UpdateOrdenPreCargado]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[UpdateOrdenPreCargado]
	@id varchar(24)
AS
BEGIN
update TD_LPAOrdenes 
set precargado = ''NO'' 
where 
idDocumento = @id and 
precargado = ''SI'' and 
ejecutado = ''NO''
END' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[InsertOrden]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[InsertOrden]
	@workflow varchar(128), 
	@idDocumento varchar(24),
	@propiedades varchar(256),
	@precargado varchar(2)
AS
BEGIN

insert into TD_LPAOrdenes (nombreWorkflow,idDocumento,valoresPropiedadesBPM,precargado,ejecutado) values (@workflow,@idDocumento,@propiedades,@precargado,''NO'')

END' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[UpdateOrden]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[UpdateOrden]
	@id int
AS
BEGIN
update TD_LPAOrdenes set ejecutado = ''SI'', fechaEjecucion = getDate() where idOrden = @id and precargado = ''NO'' and ejecutado = ''NO''
END' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectBOF]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ====================================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene BOF por clase documental
-- ====================================================================
CREATE PROCEDURE [dbo].[SelectBOF]
		@clase varchar(64)
AS
BEGIN

	SELECT  
		[lifeCycle],
		[workflow],
		[propiedadCodigo],
		[propiedadGrupos],
		[propiedadSector],
		[propiedadesUsuarios],
		[gruposExtra],
		[gruposFueraVigencia],
		[propiedadReemplazos],
		[propiedadVersion],
		[estadoVigente],
		[estadoInicial],
		[propiedadCopia]
	FROM 
		TP_LPABOF 
	WHERE 
		clase = @clase

END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[InsertWorkflowLog]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[InsertWorkflowLog]
	@usuario varchar(64), 
	@mensaje varchar(256),
	@docAdjunto varchar(64),
	@workflowID varchar(64),
	@workflowName varchar(64),
	@version varchar(8),
	@idDoc varchar(16)
AS
BEGIN

insert into TD_LPAWorkflow_Hist (idWorkflow,nombreWorkflow,mensaje,fecha,usuario,documentoAdjunto,version,idDocumento) values (@workflowID,@workflowName,@mensaje,getdate(),@usuario,@docAdjunto,@version,@idDoc)

END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectWorkflowHistoryByDocId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ======================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene historial de procesos 
-- ======================================================
CREATE PROCEDURE [dbo].[SelectWorkflowHistoryByDocId] 
	-- Add the parameters for the stored procedure here
	@codDocumento varchar(64)
AS
BEGIN

    -- Insert statements for procedure here
	SELECT 
		  [ID],
		  [idWorkflow],	      
		  [nombreWorkflow],
		  [mensaje],
		  [fecha],
		  [usuario],
		  [documentoAdjunto],      
          [version],
		  [idDocumento]
	FROM [TD_LPAWorkflow_Hist]	

	WHERE [documentoAdjunto]=@codDocumento 

	ORDER BY [idWorkflow],
		     [fecha]

END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectViewerBuscadorRelaciones]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ====================================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene info clase documental para tab doc relacionados
-- =====================================================================
CREATE PROCEDURE [dbo].[SelectViewerBuscadorRelaciones] 
	@clase 	varchar(256)
AS
BEGIN
	SELECT 
		ID,
		clase,
		descripcion, 
		propiedades,
		nombrePropiedades,
		tipoPropiedades ,
		resultados,
		nombreResultados,
		tipoResultados

		
	FROM 
		TP_LPAViewer_BuscadorRelaciones 
	WHERE 
		clase=@clase
END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectAllViewerBuscadorRelaciones]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ====================================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene clases documentales para tab doc relacionados
-- ====================================================================
CREATE PROCEDURE [dbo].[SelectAllViewerBuscadorRelaciones] 	
AS
BEGIN
	SELECT 
		ID,
		clase,
		descripcion, 
		propiedades,
		nombrePropiedades,
		tipoPropiedades ,
		resultados,
		nombreResultados,
		tipoResultados
		
	FROM 
		TP_LPAViewer_BuscadorRelaciones 
END' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectTodosVencimientos]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[SelectTodosVencimientos] 
AS
BEGIN

select clase, propiedadVencimiento, diasVencimiento, propiedadNotificado, diasNotificacion, diasPreVencimiento, workflowVencimiento, estadoVigente from TP_LPAVencimientos

END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectInfoVencimientos]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[SelectInfoVencimientos] 
	@clase varchar(64)
AS
BEGIN

select propiedadVigencia, propiedadVencimiento, diasVencimiento, propiedadNotificado, diasNotificacion, diasPreVencimiento, workflowVencimiento from TP_LPAVencimientos where clase = @clase

END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectControlImpresion]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[SelectControlImpresion] 
@clase		varchar(64),
@sector    varchar(64)
AS
BEGIN

select count(*) as cantidad from TP_LPAControl_Impresion where clase = @clase and sector = @sector

END' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectBookmarksWord]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[SelectBookmarksWord] 
	@clase varchar(64)
AS
BEGIN

select ID, clase, bookmark, propiedad, tipo from TP_LPAWorkflow_Doc where clase = @clase

END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectInfoWorkflowLog]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[SelectInfoWorkflowLog] 
	@paso varchar(64)
AS
BEGIN

select pasoAnterior, mensajeLog from TP_LPAWorkflow_Hist where nombrePaso = @paso

END

' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[InsertRelacion]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

-- ======================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Inserta una relacion 
-- ======================================================
CREATE PROCEDURE [dbo].[InsertRelacion] 
	@idOrigen varchar(16),
	@idDestino varchar(16),
	@usuario varchar(64),
	@codigoDocOrigen varchar(64),
	@codigoDocDestino varchar(64),
	@versionDocOrigen varchar(8),
	@versionDocDestino varchar(8)
AS
BEGIN

	INSERT INTO TD_LPARelaciones (
		idOrigen, 
		idDestino, 
		usuario, 
		fechaRelacion,
		codigoDocOrigen,
		codigoDocDestino,
		versionDocOrigen,
		versionDocDestino
	) 
	VALUES(
		@idOrigen, 
		@idDestino, 
		@usuario, 
		getdate(),
		@codigoDocOrigen,
		@codigoDocDestino,
		@versionDocOrigen,
		@versionDocDestino
	)

END


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectRelaciones_ORI]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

-- ======================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene relaciones de un documento
-- ======================================================
CREATE PROCEDURE [dbo].[SelectRelaciones_ORI] 
	@codigoDocOrigen varchar(64),
	@versionDocOrigen varchar(8)
AS
BEGIN

SELECT     
	idrelacion, 
	codigoDocOrigen,
	versionDocOrigen,
	idOrigen, 
	idDestino, 
	codigoDocDestino,
	versionDocDestino,
	usuario, 
	fechaRelacion
	
FROM         
	TD_LPARelaciones
WHERE	
	 (codigoDocOrigen = @codigoDocOrigen and versionDocOrigen = @versionDocOrigen) OR
	 (codigoDocDestino = @codigoDocOrigen and versionDocDestino = @versionDocOrigen) 


END


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectRelacionPorID]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

-- ======================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene una relacion por ID
-- ======================================================
CREATE PROCEDURE [dbo].[SelectRelacionPorID] 
	@idrelacion varchar(64)
AS
BEGIN

SELECT     
	
	codigoDocOrigen,
	versionDocOrigen,	
	codigoDocDestino,
	versionDocDestino,
	usuario, 
	fechaRelacion
	
FROM         
	TD_LPARelaciones
WHERE	
	idrelacion=@idrelacion


END



' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[DeleteRelaciones]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'


CREATE PROCEDURE [dbo].[DeleteRelaciones] 
	@codigo varchar(64),
	@version varchar(8)
AS
BEGIN

DELETE
FROM         
	TD_LPARelaciones
WHERE
	(codigoDocOrigen = @codigo and versionDocOrigen = @version) OR
	(codigoDocDestino = @codigo and versionDocDestino = @version)

END



' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectAnexos]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

-- ======================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene anexos de un documento
-- ======================================================
CREATE PROCEDURE [dbo].[SelectAnexos] 
	@codigoDocOrigen varchar(64),
	@versionDocOrigen varchar(8)
AS
BEGIN

SELECT     
	idrelacion, 
	codigoDocOrigen,
	versionDocOrigen,
	idOrigen, 
	idDestino, 
	codigoDocDestino,
	versionDocDestino,
	usuario, 
	fechaRelacion
	
FROM         
	TD_LPAAnexos
WHERE	
	 (codigoDocOrigen = @codigoDocOrigen and versionDocOrigen = @versionDocOrigen) OR
	 (codigoDocDestino = @codigoDocOrigen and versionDocDestino = @versionDocOrigen) 


END




' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[InsertAnexo]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

-- ======================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Inserta un anexo 
-- ======================================================
CREATE PROCEDURE [dbo].[InsertAnexo] 
	@idOrigen varchar(16),
	@idDestino varchar(16),
	@usuario varchar(64),
	@codigoDocOrigen varchar(64),
	@codigoDocDestino varchar(64),
	@versionDocOrigen varchar(8),
	@versionDocDestino varchar(8)
AS
BEGIN

	INSERT INTO TD_LPAAnexos (
		idOrigen, 
		idDestino, 
		usuario, 
		fechaRelacion,
		codigoDocOrigen,
		codigoDocDestino,
		versionDocOrigen,
		versionDocDestino
	) 
	VALUES(
		@idOrigen, 
		@idDestino, 
		@usuario, 
		getdate(),
		@codigoDocOrigen,
		@codigoDocDestino,
		@versionDocOrigen,
		@versionDocDestino
	)

END



' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[DeleteAnexos]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'



CREATE PROCEDURE [dbo].[DeleteAnexos] 
	@codigo varchar(64),
	@version varchar(8)
AS
BEGIN

DELETE
FROM         
	TD_LPAAnexos
WHERE
	(codigoDocOrigen = @codigo and versionDocOrigen = @version) OR
	(codigoDocDestino = @codigo and versionDocDestino = @version)

END




' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectAnexoPorID]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

-- ======================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene un anexo  
-- ======================================================
CREATE PROCEDURE [dbo].[SelectAnexoPorID] 
	@idrelacion varchar(64)
AS
BEGIN

SELECT     	
	codigoDocOrigen,
	versionDocOrigen,	
	codigoDocDestino,
	versionDocDestino,
	usuario, 
	fechaRelacion
	
FROM         
	TD_LPAAnexos
WHERE	
	idrelacion=@idrelacion


END


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[DeleteHistorialDocVersion]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'




CREATE PROCEDURE [dbo].[DeleteHistorialDocVersion] 
	@codigo varchar(64),
	@version varchar(8)
AS
BEGIN

DELETE
FROM         
	TD_LPAHistorial_Documento
WHERE
	codigo = @codigo and version = @version

END





' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectHistorialDocumento]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

-- ======================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene Historial de un documento
-- ======================================================
CREATE PROCEDURE [dbo].[SelectHistorialDocumento] 
	@codigo varchar(64),
	@version varchar(8)
AS
BEGIN

SELECT     
	ID, 		
	codigo,
	version,
	evento,
	usuario, 
	fecha
	
FROM         
	TD_LPAHistorial_Documento
WHERE	
	codigo = @codigo 
	and version = @version
	  

END


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[InsertHistorialDocumento]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'

-- ======================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Inserta Hisrotial de un documento
-- ======================================================
CREATE PROCEDURE [dbo].[InsertHistorialDocumento] 	
	@codigo varchar(64),
	@evento varchar(64),
	@usuario varchar(64),
	@version varchar(8)
	
AS
BEGIN

	INSERT INTO TD_LPAHistorial_Documento (		
		codigo,
		version,
		evento,
		usuario, 
		fecha
	) 
	VALUES(		
		@codigo,
		@version,
		@evento,
		@usuario, 
		getdate()
	)

END


' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectWorkflowBPM]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE [dbo].[SelectWorkflowBPM] 
	@workflow 	varchar(64)
AS
BEGIN

select packageAdjunto, packageBPM, claseBPM, propiedades from TP_LPAWorkflow_BPM where workflow = @workflow 

END



' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SelectWorkflowCommentsByDocId]') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'
-- ======================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene distoria de procesos 
-- ======================================================
CREATE PROCEDURE [dbo].[SelectWorkflowCommentsByDocId] 
	-- Add the parameters for the stored procedure here
	@codDocumento varchar(32)
AS
BEGIN


    -- Insert statements for procedure here
	SELECT
		[idComentario],
        [nombreWorkflow],
		[version]
        
        
	FROM 
		[TD_LPAWorkflow_Comments]	

	WHERE 
		[codDocumento]=@codDocumento

	ORDER BY 
		[nombreWorkflow]

END
' 
END
