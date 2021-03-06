USE [TD_documentum]
GO
/****** Object:  StoredProcedure [dbo].[SelectDescargas]    Script Date: 04/18/2012 09:56:08 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[SelectDescargas] 
    @clase varchar(64) ='at' ,
	@dosExtension varchar(5) ='cdr'
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
GO
/****** Object:  Table [dbo].[TP_LPADescargas]    Script Date: 04/18/2012 09:56:42 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPADescargas](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[dosExtension] [varchar](5) NOT NULL,
 CONSTRAINT [PK_TP_LPADescargas] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TD_LPAControl_Impresion]    Script Date: 04/18/2012 09:56:18 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TD_LPAControl_Impresion](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[codigo] [varchar](64) NOT NULL,
	[version] [varchar](8) NOT NULL,
	[grupo] [varchar](64) NOT NULL,
	[cantCopias] [int] NOT NULL,
	[tipoCopia] [int] NOT NULL,
	[permitir] [varchar](2) NOT NULL,
 CONSTRAINT [PK_TD_LPAControl_Impresion] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  StoredProcedure [dbo].[SelectImpresoras]    Script Date: 04/18/2012 09:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[SelectImpresoras] 
	
AS
BEGIN

SELECT  
	ID,
	nombreImpresora
	
FROM         
	TV_LPAImpresoras

END
GO
/****** Object:  StoredProcedure [dbo].[SelectTipoCopias]    Script Date: 04/18/2012 09:56:11 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
		comboDesplegable = 'SI'

END
GO
/****** Object:  StoredProcedure [dbo].[HistorialImpresion]    Script Date: 04/18/2012 09:56:01 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
inner join inq..activitylog al on dv.id = al.iddoc and (sidactdf = 'print' or sidactdf = 'printControlled')
inner join inq..participantdef pd on al.idusrdf = pd.id 
where 
siddoc like @CodDoc and version = @version


END
GO
/****** Object:  StoredProcedure [dbo].[Sp_InfoDocInQ]    Script Date: 04/18/2012 09:56:13 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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

--select top 1 * from documentversion where siddoc = 'SOP-CC-000039' order by version desc
-- Paso 1: Se obtiene el id del documento (VER SI ESTAN VIGENTE)
select @lId = id from inq..documentversion where siddoc like @CodDoc and version = @version

-- Paso 2: Obtengo las fecha de revision, aprobacion, publicacion y edicion
--todo:select * from activitylog where iddoc = 494713 order by datecompleted
select @sFechaEdicion = datecompleted from inq..activitylog where iddoc = @lId and sidactdf = 'Edition' and datecompleted = (select max(datecompleted) from inq..activitylog where iddoc = @lId and sidactdf = 'Edition') 
select @sFechaRevision = datecompleted  from inq..activitylog where iddoc = @lId and sidactdf = 'review' and datecompleted = (select max(datecompleted) from inq..activitylog where iddoc = @lId and sidactdf = 'review') 
select @sFechaAprobacion = datecompleted from inq..activitylog where iddoc = @lId and sidactdf = 'approval' and datecompleted = (select max(datecompleted) from inq..activitylog where iddoc = @lId and sidactdf = 'approval') 
select @sFechaPublicacion = datecompleted from inq..activitylog where iddoc = @lId and sidactdf = 'publication' and datecompleted = (select max(datecompleted) from inq..activitylog where iddoc = @lId and sidactdf = 'publication') 

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
GO
/****** Object:  Table [dbo].[TD_LPAServiceImpresion]    Script Date: 04/18/2012 09:56:29 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TD_LPAServiceImpresion](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[idDocumento] [varchar](16) NOT NULL,
	[idImpresion] [int] NOT NULL,
	[usuario] [varchar](64) NOT NULL,
	[impresora] [int] NOT NULL,
	[tipoCopia] [int] NOT NULL,
	[procesada] [varchar](2) NOT NULL CONSTRAINT [DF_TD_LPAServiceImpresion_realizada]  DEFAULT ('NO'),
	[fechaImpresion] [datetime] NULL,
	[mensajeError] [varchar](256) NULL,
	[CantCopias] [int] NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TP_LPAOpenDTS]    Script Date: 04/18/2012 09:56:46 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPAOpenDTS](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[MarcaAgua] [varchar](2) NOT NULL,
	[texto] [varchar](128) NOT NULL,
 CONSTRAINT [PK_TP_LPAOpenDTS] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  StoredProcedure [dbo].[SelectViewerBotones]    Script Date: 04/18/2012 09:56:11 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
	SET  @sQuery = ''


    -- Select Botones Viewer------
	SET  @sQuery = @sQuery + 'SELECT ID,clase,grupo,botones  FROM TP_LPAViewer_Botones '
	SET  @sQuery = @sQuery + 'WHERE clase = '''+@clase+''''+' and grupo like ''%'+@selector+'%'' and ' 
	SET  @sQuery = @sQuery + 'grupo in ('+@sGrupos +')'

	EXEC sp_sqlexec @sQuery
		 
	PRINT @sQuery
	

END
GO
/****** Object:  StoredProcedure [dbo].[DeleteAnexo]    Script Date: 04/18/2012 09:56:00 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  Table [dbo].[TP_LPAViewer_BuscadorAnexos]    Script Date: 04/18/2012 09:56:54 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPAViewer_BuscadorAnexos](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](256) NOT NULL,
	[descripcion] [varchar](256) NOT NULL,
	[propiedades] [varchar](1024) NOT NULL,
	[nombrePropiedades] [varchar](2048) NOT NULL,
	[tipoPropiedades] [varchar](1024) NOT NULL,
	[resultados] [varchar](1024) NOT NULL,
	[nombreResultados] [varchar](2048) NOT NULL,
	[tipoResultados] [varchar](1024) NOT NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TP_LPAWorkflow_Mail]    Script Date: 04/18/2012 09:57:03 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPAWorkflow_Mail](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[nombrePaso] [varchar](64) NOT NULL,
	[subject] [varchar](128) NOT NULL,
	[body] [varchar](256) NOT NULL,
	[ACL] [varchar](2) NOT NULL,
	[propiedadBPM] [varchar](64) NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TP_LPAWorkflow_Tasks]    Script Date: 04/18/2012 09:57:04 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPAWorkflow_Tasks](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[nombreWorkflow] [varchar](128) NOT NULL,
	[nombrePaso] [varchar](128) NOT NULL,
	[propiedadUsuarios] [varchar](128) NOT NULL,
	[documento] [varchar](6) NOT NULL,
 CONSTRAINT [PK_TP_LPAWorkflow_Tasks] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  StoredProcedure [dbo].[InsertHistorialImpresionMigracion]    Script Date: 04/18/2012 09:56:04 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[InsertHistorialImpresionMigracion]
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
GO
/****** Object:  StoredProcedure [dbo].[SelectViewerBotonesPorGrupo]    Script Date: 04/18/2012 09:56:12 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
	SET  @sQuery = ''

    -- Insert statements for procedure here	
	SET  @sQuery = @sQuery + 'SELECT botones  FROM TP_LPAViewer_Botones_Grupo '
	SET  @sQuery = @sQuery + 'WHERE grupo in ('+@sGrupos +')'

	EXEC sp_sqlexec @sQuery
		 
	PRINT @sQuery
	

END
GO
/****** Object:  Table [dbo].[TP_LPAViewer_Botones_Grupo]    Script Date: 04/18/2012 09:56:52 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPAViewer_Botones_Grupo](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[grupo] [varchar](64) NOT NULL,
	[botones] [varchar](32) NOT NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TD_LPAOrdenes]    Script Date: 04/18/2012 09:56:25 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TD_LPAOrdenes](
	[idOrden] [int] IDENTITY(1,1) NOT NULL,
	[nombreWorkflow] [varchar](128) NOT NULL,
	[idDocumento] [varchar](16) NULL,
	[precargado] [varchar](2) NOT NULL,
	[ejecutado] [varchar](2) NOT NULL,
	[valoresPropiedadesBPM] [varchar](256) NULL,
	[fechaEjecucion] [datetime] NULL,
 CONSTRAINT [PK_TD_LPAOrdenes] PRIMARY KEY CLUSTERED 
(
	[idOrden] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TP_LPABOF_back]    Script Date: 04/18/2012 09:56:40 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPABOF_back](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[lifeCycle] [varchar](128) NOT NULL,
	[workflow] [varchar](128) NOT NULL,
	[propiedadCodigo] [varchar](64) NULL,
	[propiedadGrupos] [varchar](64) NULL,
	[propiedadSector] [varchar](64) NULL,
	[propiedadesUsuarios] [varchar](128) NULL,
	[gruposExtra] [varchar](128) NULL,
	[gruposFueraVigencia] [varchar](128) NULL,
	[propiedadReemplazos] [varchar](64) NULL,
	[propiedadVersion] [varchar](64) NULL,
	[estadoVigente] [varchar](64) NULL,
	[estadoInicial] [varchar](64) NULL,
	[propiedadCopia] [varchar](64) NULL,
 CONSTRAINT [PK_TP_LPABOF] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TD_LPAWorkflow_Hist]    Script Date: 04/18/2012 09:56:33 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TD_LPAWorkflow_Hist](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[idWorkflow] [varchar](32) NOT NULL,
	[nombreWorkflow] [varchar](64) NOT NULL,
	[mensaje] [varchar](256) NOT NULL,
	[fecha] [datetime] NOT NULL,
	[usuario] [varchar](64) NOT NULL,
	[documentoAdjunto] [varchar](64) NOT NULL,
	[version] [varchar](8) NOT NULL,
	[idDocumento] [varchar](16) NOT NULL,
 CONSTRAINT [PK_TD_LPAWorkflow_Hist] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TP_LPAViewer_BuscadorRelaciones]    Script Date: 04/18/2012 09:56:57 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPAViewer_BuscadorRelaciones](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](256) NOT NULL,
	[descripcion] [varchar](256) NOT NULL,
	[propiedades] [varchar](1024) NOT NULL,
	[nombrePropiedades] [varchar](2048) NOT NULL,
	[tipoPropiedades] [varchar](1024) NOT NULL,
	[resultados] [varchar](1024) NOT NULL,
	[nombreResultados] [varchar](2048) NOT NULL,
	[tipoResultados] [varchar](1024) NOT NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TP_LPAVencimientos]    Script Date: 04/18/2012 09:56:50 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPAVencimientos](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[propiedadVigencia] [varchar](128) NOT NULL,
	[propiedadVencimiento] [varchar](128) NOT NULL,
	[diasVencimiento] [int] NOT NULL,
	[propiedadNotificado] [varchar](128) NOT NULL,
	[diasNotificacion] [int] NOT NULL,
	[diasPreVencimiento] [int] NOT NULL,
	[workflowVencimiento] [varchar](128) NOT NULL,
	[estadoVigente] [varchar](32) NOT NULL,
 CONSTRAINT [PK_TP_LPAVencimientos] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TP_LPAControl_Impresion]    Script Date: 04/18/2012 09:56:41 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPAControl_Impresion](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[sector] [varchar](64) NOT NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TP_LPAWorkflow_Doc]    Script Date: 04/18/2012 09:57:00 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPAWorkflow_Doc](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[bookmark] [varchar](64) NOT NULL,
	[propiedad] [varchar](64) NOT NULL,
	[tipo] [varchar](32) NOT NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TP_LPAWorkflow_Hist]    Script Date: 04/18/2012 09:57:01 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPAWorkflow_Hist](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[nombrePaso] [varchar](64) NOT NULL,
	[mensajeLog] [varchar](256) NOT NULL,
	[pasoAnterior] [varchar](64) NULL,
 CONSTRAINT [PK_TP_LPAWorkflow_Hist] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  StoredProcedure [dbo].[SelectImpresorasPorGrupo]    Script Date: 04/18/2012 09:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
	SET  @sQuery = ''
    
	--Select impresoras--------------
	SET  @sQuery = @sQuery + 'SELECT ID,nombreImpresora FROM TV_LPAImpresoras '
	SET  @sQuery = @sQuery + 'WHERE grupo in ('+@sGrupos +')'

	EXEC sp_sqlexec @sQuery
		 
	PRINT @sQuery

END
GO
/****** Object:  StoredProcedure [dbo].[DeleteRelacion]    Script Date: 04/18/2012 09:56:01 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[IncrementaNumeroNomenclador]    Script Date: 04/18/2012 09:56:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[IncrementaNumeroNomenclador]
	@clase varchar(64), 
	@parametros varchar(1024)
AS
BEGIN

declare @vsql Nvarchar(2048)

set @vsql = 'update TD_LPANomenclador set numerador = numerador + 1 where clase = ''' + @clase + '''' + ISNULL(@parametros, '')

exec sp_sqlexec @vsql

END
GO
/****** Object:  StoredProcedure [dbo].[InsertWorkflowComments]    Script Date: 04/18/2012 09:56:05 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  Table [dbo].[TV_LPAImpresoras]    Script Date: 04/18/2012 09:57:06 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TV_LPAImpresoras](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[nombreImpresora] [varchar](128) NOT NULL,
	[grupo] [varchar](64) NOT NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  StoredProcedure [dbo].[BloqueoWord]    Script Date: 04/18/2012 09:56:00 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[BloqueoWord] 
AS
BEGIN
update TV_LPABotones set Descripcion = (select Descripcion from TV_LPABotones where ID = 1) where ID = 1
END
GO
/****** Object:  Table [dbo].[TD_LPARelaciones]    Script Date: 04/18/2012 09:56:27 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TD_LPARelaciones](
	[idrelacion] [int] IDENTITY(1,1) NOT NULL,
	[idOrigen] [varchar](16) NOT NULL,
	[idDestino] [varchar](16) NOT NULL,
	[usuario] [varchar](64) NOT NULL,
	[fechaRelacion] [datetime] NOT NULL,
	[codigoDocOrigen] [varchar](64) NOT NULL,
	[versionDocOrigen] [varchar](8) NOT NULL,
	[codigoDocDestino] [varchar](64) NOT NULL,
	[versionDocDestino] [varchar](8) NOT NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  StoredProcedure [dbo].[SelectReglasNomenclador]    Script Date: 04/18/2012 09:56:10 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
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
GO
/****** Object:  Table [dbo].[02_Hernan]    Script Date: 04/18/2012 09:56:14 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[02_Hernan](
	[test] [nchar](10) NULL
) ON [PRIMARY]
GO
/****** Object:  StoredProcedure [dbo].[SelectNumeroNomenclador]    Script Date: 04/18/2012 09:56:10 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
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

set @vsql = 'select numerador from TD_LPANomenclador where clase = ''' + @clase + '''' + ISNULL(@parametros, '')

exec sp_sqlexec @vsql

END
GO
/****** Object:  Table [dbo].[TD_LPAAnexos]    Script Date: 04/18/2012 09:56:16 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TD_LPAAnexos](
	[idrelacion] [int] IDENTITY(1,1) NOT NULL,
	[idOrigen] [varchar](16) NOT NULL,
	[idDestino] [varchar](16) NOT NULL,
	[usuario] [varchar](64) NOT NULL,
	[fechaRelacion] [datetime] NOT NULL,
	[codigoDocOrigen] [varchar](64) NOT NULL,
	[versionDocOrigen] [varchar](8) NOT NULL,
	[codigoDocDestino] [varchar](64) NOT NULL,
	[versionDocDestino] [varchar](8) NOT NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TD_LPAHistorial_Documento]    Script Date: 04/18/2012 09:56:19 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TD_LPAHistorial_Documento](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[codigo] [varchar](64) NOT NULL,
	[version] [varchar](8) NOT NULL,
	[evento] [varchar](64) NOT NULL,
	[usuario] [varchar](64) NOT NULL,
	[fecha] [datetime] NOT NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  StoredProcedure [dbo].[UpdateImpresion]    Script Date: 04/18/2012 09:56:13 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[UpdateImpresion]
	@id int
AS
BEGIN
update TD_LPAServiceImpresion set procesada = 'SI', fechaImpresion = getDate() where ID = @id and procesada = 'NO'
END
GO
/****** Object:  Table [dbo].[TP_LPANomenclador]    Script Date: 04/18/2012 09:56:45 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPANomenclador](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](256) NOT NULL,
	[propiedadCodigo] [varchar](64) NOT NULL,
	[propiedadNumerador] [varchar](64) NULL,
	[numerador] [varchar](2) NOT NULL CONSTRAINT [DF_TP_LPANomenclador_numerador]  DEFAULT ('NO'),
	[propiedades] [varchar](1024) NOT NULL,
	[tipoPropiedades] [varchar](1024) NOT NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TD_LPANomenclador]    Script Date: 04/18/2012 09:56:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TD_LPANomenclador](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](128) NOT NULL,
	[p_sector] [varchar](128) NULL,
	[p_tipodoc] [varchar](128) NULL,
	[numerador] [int] NOT NULL CONSTRAINT [DF_TD_LPANomenclador_numerador]  DEFAULT ((0))
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TD_LPAHistorial_Impresion]    Script Date: 04/18/2012 09:56:22 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TD_LPAHistorial_Impresion](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[usuario] [varchar](64) NOT NULL,
	[fecha] [datetime] NOT NULL,
	[tipoCopia] [int] NOT NULL,
	[idImpresora] [int] NOT NULL,
	[comentario] [varchar](256) NULL,
	[codigoDoc] [varchar](64) NOT NULL,
	[version] [varchar](8) NOT NULL,
	[idDocumento] [varchar](16) NOT NULL,
	[CantCopias] [int] NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TP_LPATipoCopias]    Script Date: 04/18/2012 09:56:47 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPATipoCopias](
	[ID] [int] NOT NULL,
	[tipoCopia] [varchar](64) NOT NULL,
	[controlImpresion] [varchar](2) NOT NULL,
	[comboDesplegable] [varchar](2) NOT NULL,
	[leyendaWord] [varchar](128) NOT NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  StoredProcedure [dbo].[SelectGruposSinControl]    Script Date: 04/18/2012 09:56:08 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
	SET  @sQuery = ''
    
	--Select impresoras--------------
	SET  @sQuery = @sQuery + 'SELECT count(ID) as cantidad FROM TP_LPAGrupos_SinControl '
	SET  @sQuery = @sQuery + 'WHERE grupo in ('+@sGrupos +')'

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
)
GO
/****** Object:  Table [dbo].[TD_LPAWorkflow_Comments]    Script Date: 04/18/2012 09:56:31 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TD_LPAWorkflow_Comments](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[nombreWorkflow] [varchar](128) NOT NULL,
	[idDocumento] [varchar](16) NOT NULL,
	[codDocumento] [varchar](32) NOT NULL,
	[version] [varchar](8) NOT NULL,
	[idComentario] [varchar](16) NOT NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TP_LPAGrupos_SinControl]    Script Date: 04/18/2012 09:56:43 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPAGrupos_SinControl](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[grupo] [varchar](64) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TP_LPAViewer_Botones]    Script Date: 04/18/2012 09:56:51 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPAViewer_Botones](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[grupo] [varchar](64) NOT NULL,
	[botones] [varchar](32) NOT NULL,
 CONSTRAINT [PK_TP_LPAViewer_Botones] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TV_LPABotones]    Script Date: 04/18/2012 09:57:05 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TV_LPABotones](
	[ID] [int] NOT NULL,
	[NombreBoton] [varchar](64) NOT NULL,
	[Descripcion] [varchar](128) NOT NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  View [dbo].[v_t_gescal_vigentes]    Script Date: 04/18/2012 09:57:06 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[v_t_gescal_vigentes]
AS
SELECT     TOP (100) PERCENT r_object_id, p_estado AS estado, p_codigo, p_version
FROM         DM_documentum_docbase.dbo.t_gescal_s
WHERE     (p_codigo <> '') AND (p_estado <> '') AND (p_estado <> 'PreEdicion')
ORDER BY p_codigo
GO
EXEC sys.sp_addextendedproperty @name=N'MS_DiagramPane1', @value=N'[0E232FF0-B466-11cf-A24F-00AA00A3EFFF, 1.00]
Begin DesignProperties = 
   Begin PaneConfigurations = 
      Begin PaneConfiguration = 0
         NumPanes = 4
         Configuration = "(H (1[40] 4[20] 2[20] 3) )"
      End
      Begin PaneConfiguration = 1
         NumPanes = 3
         Configuration = "(H (1 [50] 4 [25] 3))"
      End
      Begin PaneConfiguration = 2
         NumPanes = 3
         Configuration = "(H (1[38] 2[11] 3) )"
      End
      Begin PaneConfiguration = 3
         NumPanes = 3
         Configuration = "(H (4 [30] 2 [40] 3))"
      End
      Begin PaneConfiguration = 4
         NumPanes = 2
         Configuration = "(H (1 [56] 3))"
      End
      Begin PaneConfiguration = 5
         NumPanes = 2
         Configuration = "(H (2 [66] 3))"
      End
      Begin PaneConfiguration = 6
         NumPanes = 2
         Configuration = "(H (4 [50] 3))"
      End
      Begin PaneConfiguration = 7
         NumPanes = 1
         Configuration = "(V (3))"
      End
      Begin PaneConfiguration = 8
         NumPanes = 3
         Configuration = "(H (1[56] 4[18] 2) )"
      End
      Begin PaneConfiguration = 9
         NumPanes = 2
         Configuration = "(H (1 [75] 4))"
      End
      Begin PaneConfiguration = 10
         NumPanes = 2
         Configuration = "(H (1[66] 2) )"
      End
      Begin PaneConfiguration = 11
         NumPanes = 2
         Configuration = "(H (4 [60] 2))"
      End
      Begin PaneConfiguration = 12
         NumPanes = 1
         Configuration = "(H (1) )"
      End
      Begin PaneConfiguration = 13
         NumPanes = 1
         Configuration = "(V (4))"
      End
      Begin PaneConfiguration = 14
         NumPanes = 1
         Configuration = "(V (2))"
      End
      ActivePaneConfig = 2
   End
   Begin DiagramPane = 
      Begin Origin = 
         Top = 0
         Left = 0
      End
      Begin Tables = 
         Begin Table = "t_gescal_s (DM_documentum_docbase.dbo)"
            Begin Extent = 
               Top = 6
               Left = 38
               Bottom = 114
               Right = 254
            End
            DisplayFlags = 280
            TopColumn = 0
         End
      End
   End
   Begin SQLPane = 
   End
   Begin DataPane = 
      Begin ParameterDefaults = ""
      End
      Begin ColumnWidths = 9
         Width = 284
         Width = 1560
         Width = 1035
         Width = 1590
         Width = 1500
         Width = 1500
         Width = 1500
         Width = 1500
         Width = 1500
      End
   End
   Begin CriteriaPane = 
      PaneHidden = 
      Begin ColumnWidths = 11
         Column = 1440
         Alias = 900
         Table = 1170
         Output = 720
         Append = 1400
         NewValue = 1170
         SortType = 1350
         SortOrder = 1410
         GroupBy = 1350
         Filter = 1350
         Or = 1350
         Or = 1350
         Or = 1350
      End
   End
End
' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'VIEW',@level1name=N'v_t_gescal_vigentes'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_DiagramPaneCount', @value=1 , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'VIEW',@level1name=N'v_t_gescal_vigentes'
GO
/****** Object:  Table [dbo].[TP_LPAWorkflow_BPM]    Script Date: 04/18/2012 09:56:58 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPAWorkflow_BPM](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[workflow] [varchar](128) NOT NULL,
	[packageAdjunto] [varchar](64) NOT NULL,
	[packageBPM] [varchar](64) NULL,
	[claseBPM] [varchar](64) NULL,
	[propiedades] [varchar](256) NULL,
 CONSTRAINT [PK_TP_LPAWorkflow_BPM] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TP_LPABOF]    Script Date: 04/18/2012 09:56:37 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TP_LPABOF](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[clase] [varchar](64) NOT NULL,
	[lifeCycle] [varchar](128) NOT NULL,
	[workflow] [varchar](128) NOT NULL,
	[propiedadCodigo] [varchar](64) NULL,
	[propiedadGrupos] [varchar](64) NULL,
	[propiedadSector] [varchar](64) NULL,
	[propiedadesUsuarios] [varchar](128) NULL,
	[gruposExtra] [varchar](128) NULL,
	[gruposFueraVigencia] [varchar](128) NULL,
	[propiedadReemplazos] [varchar](64) NULL,
	[propiedadVersion] [varchar](64) NULL,
	[estadoVigente] [varchar](64) NULL,
	[estadoInicial] [varchar](64) NULL,
	[propiedadCopia] [varchar](64) NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  StoredProcedure [dbo].[SelectCantidadCopias]    Script Date: 04/18/2012 09:56:07 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
        PRINT 'PUEDES REALIZAR UNA COPIA CONTROLADA ' + CONVERT(VARCHAR(2),@retorno) 
        
	END 
	ELSE IF @cantCopias=1 BEGIN        
        SET @retorno = 1
        PRINT 'SE DEBE EJECUTAR EL PROCESO INSERTAR ORDEN ' + CONVERT(VARCHAR(2),@retorno) 
        
	END 
 
END
GO
/****** Object:  StoredProcedure [dbo].[DeshabilitaImpresion]    Script Date: 04/18/2012 09:56:01 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[DeshabilitaImpresion]
	@codigo varchar(64)
AS
BEGIN
update TD_LPAControl_Impresion set permitir = 'NO' where codigo = @codigo
END
GO
/****** Object:  StoredProcedure [dbo].[HabilitaImpresion]    Script Date: 04/18/2012 09:56:01 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[HabilitaImpresion]
	@codigo varchar(64),
	@grupo varchar(64)
AS
BEGIN
update TD_LPAControl_Impresion set permitir = 'SI' where codigo = @codigo and grupo = @grupo
END
GO
/****** Object:  StoredProcedure [dbo].[RenuevaImpresion]    Script Date: 04/18/2012 09:56:06 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[RenuevaImpresion]
	@codigo varchar(64),
	@grupo varchar(64),
	@version varchar(8)
AS
BEGIN
update TD_LPAControl_Impresion set cantCopias = 0 where codigo = @codigo and grupo = @grupo and version = @version
END
GO
/****** Object:  StoredProcedure [dbo].[InsertControlImpresion]    Script Date: 04/18/2012 09:56:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
    SELECT @query4 = count(ID)  FROM TD_LPAControl_Impresion WHERE codigo = @codigo and grupo = @grupo and permitir = 'NO' and tipoCopia=@tipoCopia
    SELECT @query5 = cantCopias FROM TD_LPAControl_Impresion WHERE codigo = @codigo and grupo = @grupo and version = @version and tipoCopia=@tipoCopia        

    --Query 2
    SET @sInsert  = ''
    SET @sInsert = @sInsert + 'INSERT INTO TD_LPAControl_Impresion (codigo,version,grupo,cantCopias,tipoCopia,permitir) '
    SET @sInsert = @sInsert + 'VALUES('''+@codigo+''', '''+@version+''', '''+@grupo+''',1,'+LTRIM(RTRIM(CAST(@tipoCopia AS VARCHAR(2))))+',''SI'')'
    
    --Query 6
    SET @sUpdate  = ''
    SET @sUpdate = @sUpdate + 'update TD_LPAControl_Impresion set cantCopias = cantCopias+1 where codigo = ''' +@codigo+ ''''
    SET @sUpdate = @sUpdate + 'and grupo = '''+@grupo+''' and version = '''+@version+''' and tipoCopia = '+LTRIM(RTRIM(CAST(@tipoCopia AS VARCHAR(2))))

    --Validaciones    
    IF @query1=0 BEGIN
        SET @sInsert = @sInsert
        EXEC sp_sqlexec @sInsert
        SET @retorno = 1
        PRINT 'QUERY 1 ES 0 ' + CONVERT(VARCHAR(2),@retorno) 
        
    END 
    ELSE IF @query1>0    BEGIN        
        IF @query3>0 BEGIN            
            IF @query4 >0 BEGIN            
                SET @retorno = -1    
                --RAISERROR('No Se permite la impresión', 16, 1)
                --RETURN
            END                         
            ELSE IF @query4=0 BEGIN
                    IF @query5=0 BEGIN
                        SET @sUpdate = @sUpdate
                        EXEC sp_sqlexec @sUpdate
                        SET @retorno = 1
                        PRINT 'QUERY 5 ES 0 '+CONVERT(VARCHAR(2),@retorno) 
                    END 
                    ELSE IF @query5>0 BEGIN
                        SET @retorno = -1
                        --RAISERROR('No Se permite la impresión', 16, 1)
                        --RETURN
                    END
            END 
        END    
        ELSE IF @query3=0 BEGIN 
			IF @query4 >0 BEGIN            
                SET @retorno = -1    
                --RAISERROR('No Se permite la impresión', 16, 1)
                --RETURN
            END                         
            ELSE BEGIN              
				SET @sInsert = @sInsert
				EXEC sp_sqlexec @sInsert
				SET @retorno = 1
				PRINT 'QUERY 5 ES 0 '+CONVERT(VARCHAR(2),@retorno)
			END 
        END
    END    
    
    SET    @retorno=@retorno
    PRINT @retorno
 
END
GO
/****** Object:  StoredProcedure [dbo].[InsertServiceImpresion]    Script Date: 04/18/2012 09:56:05 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- ====================================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Update date:	16.04.2012
-- Description: Inserta Servicio de impresion
-- ====================================================================
CREATE PROCEDURE [dbo].[InsertServiceImpresion] 
	-- Add the parameters for the stored procedure here
	@idDocumento varchar(16), 
	@idImpresion int,	
	@usuario varchar(64), 
	@impresora varchar(128), 
	@tipoCopia int,
	@cantCopias int
	
AS
BEGIN

    -- Insert statements for procedure here	
	INSERT INTO TD_LPAServiceImpresion (
		idDocumento,
		idImpresion ,		
		usuario, 
		impresora, 
		tipoCopia, 
		procesada,
		CantCopias
	) 
	VALUES(
		@idDocumento, 
		@idImpresion,		
		@usuario, 
		@impresora, 
		@tipoCopia,
		'NO',
		@cantCopias
	)

END
GO
/****** Object:  StoredProcedure [dbo].[SelectImpresiones]    Script Date: 04/18/2012 09:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[SelectImpresiones]
AS
BEGIN

select a.ID, a.idDocumento, a.usuario, c.nombreImpresora, b.tipoCopia, b.leyendaWord,a.CantCopias
from TD_LPAServiceImpresion a, TP_LPATipoCopias b, TV_LPAImpresoras c
where a.procesada = 'NO'
and a.impresora = c.ID
and a.tipoCopia = b.ID

END
GO
/****** Object:  StoredProcedure [dbo].[UpdateImpresionConError]    Script Date: 04/18/2012 09:56:13 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[UpdateImpresionConError]
	@id int,
	@mensaje varchar(256)
AS
BEGIN
update TD_LPAServiceImpresion set procesada = 'ER', mensajeError = @mensaje, fechaImpresion = getDate() where ID = @id and procesada = 'NO'
END
GO
/****** Object:  StoredProcedure [dbo].[SelectViewerBuscadorAnexos]    Script Date: 04/18/2012 09:56:12 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[SelectAllViewerBuscadorAnexos]    Script Date: 04/18/2012 09:56:06 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[SelectInfoWorkflowMail]    Script Date: 04/18/2012 09:56:10 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[SelectInfoWorkflowMail] 
	@paso varchar(64)
AS
BEGIN

select subject, body, ACL, propiedadBPM from TP_LPAWorkflow_Mail where nombrePaso = @paso

END
GO
/****** Object:  StoredProcedure [dbo].[SelectWorkflowTasks]    Script Date: 04/18/2012 09:56:13 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[SelectWorkflowTasks]
	@nombreWorkflow varchar(128)
AS
BEGIN
select nombrePaso, propiedadUsuarios, documento from TP_LPAWorkflow_Tasks where nombreWorkflow = @nombreWorkflow
END
GO
/****** Object:  StoredProcedure [dbo].[UpdateOrden]    Script Date: 04/18/2012 09:56:14 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[UpdateOrden]
	@id int
AS
BEGIN
update TD_LPAOrdenes set ejecutado = 'SI', fechaEjecucion = getDate() where idOrden = @id and precargado = 'NO' and ejecutado = 'NO'
END
GO
/****** Object:  StoredProcedure [dbo].[DeleteOrden]    Script Date: 04/18/2012 09:56:01 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[DeleteOrden]
	@id varchar(24)
	
AS
BEGIN

delete from TD_LPAOrdenes 
where idDocumento = @id and precargado = 'SI' and ejecutado = 'NO'

END
GO
/****** Object:  StoredProcedure [dbo].[SelectOrders]    Script Date: 04/18/2012 09:56:10 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[SelectOrders]
AS
BEGIN
select idOrden, nombreWorkflow, idDocumento, valoresPropiedadesBPM from TD_LPAOrdenes where precargado = 'NO' and ejecutado = 'NO'
END
GO
/****** Object:  StoredProcedure [dbo].[UpdateOrdenPreCargado]    Script Date: 04/18/2012 09:56:14 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[UpdateOrdenPreCargado]
	@id varchar(24)
AS
BEGIN
update TD_LPAOrdenes 
set precargado = 'NO' 
where 
idDocumento = @id and 
precargado = 'SI' and 
ejecutado = 'NO'
END
GO
/****** Object:  StoredProcedure [dbo].[InsertOrden]    Script Date: 04/18/2012 09:56:04 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[InsertOrden]
	@workflow varchar(128), 
	@idDocumento varchar(24),
	@propiedades varchar(256),
	@precargado varchar(2)
AS
BEGIN

insert into TD_LPAOrdenes (nombreWorkflow,idDocumento,valoresPropiedadesBPM,precargado,ejecutado) values (@workflow,@idDocumento,@propiedades,@precargado,'NO')

END
GO
/****** Object:  StoredProcedure [dbo].[SelectBOF]    Script Date: 04/18/2012 09:56:07 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[InsertWorkflowLog]    Script Date: 04/18/2012 09:56:06 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
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
GO
/****** Object:  StoredProcedure [dbo].[SelectWorkflowHistoryByDocId]    Script Date: 04/18/2012 09:56:13 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[SelectViewerBuscadorRelaciones]    Script Date: 04/18/2012 09:56:12 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[SelectAllViewerBuscadorRelaciones]    Script Date: 04/18/2012 09:56:06 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
END
GO
/****** Object:  StoredProcedure [dbo].[SelectTodosVencimientos]    Script Date: 04/18/2012 09:56:11 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[SelectTodosVencimientos] 
AS
BEGIN

select clase, propiedadVencimiento, diasVencimiento, propiedadNotificado, diasNotificacion, diasPreVencimiento, workflowVencimiento, estadoVigente from TP_LPAVencimientos

END
GO
/****** Object:  StoredProcedure [dbo].[SelectInfoVencimientos]    Script Date: 04/18/2012 09:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[SelectInfoVencimientos] 
	@clase varchar(64)
AS
BEGIN

select propiedadVigencia, propiedadVencimiento, diasVencimiento, propiedadNotificado, diasNotificacion, diasPreVencimiento, workflowVencimiento from TP_LPAVencimientos where clase = @clase

END
GO
/****** Object:  StoredProcedure [dbo].[SelectControlImpresion]    Script Date: 04/18/2012 09:56:07 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[SelectControlImpresion] 
@clase		varchar(64),
@sector    varchar(64)
AS
BEGIN

select count(*) as cantidad from TP_LPAControl_Impresion where clase = @clase and sector = @sector

END
GO
/****** Object:  StoredProcedure [dbo].[SelectBookmarksWord]    Script Date: 04/18/2012 09:56:07 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[SelectBookmarksWord] 
	@clase varchar(64)
AS
BEGIN

select ID, clase, bookmark, propiedad, tipo from TP_LPAWorkflow_Doc where clase = @clase

END
GO
/****** Object:  StoredProcedure [dbo].[SelectInfoWorkflowLog]    Script Date: 04/18/2012 09:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
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
GO
/****** Object:  StoredProcedure [dbo].[SelectHistorialImpresion]    Script Date: 04/18/2012 09:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- ====================================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Update date:	16.04.2012
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
		H.[CantCopias],
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
GO
/****** Object:  StoredProcedure [dbo].[SelectRelaciones_ORI]    Script Date: 04/18/2012 09:56:11 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[InsertRelacion]    Script Date: 04/18/2012 09:56:04 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[SelectRelacionPorID]    Script Date: 04/18/2012 09:56:11 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  View [dbo].[v_relaciones_estados]    Script Date: 04/18/2012 09:57:06 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[v_relaciones_estados]
AS
SELECT     TOP (100) PERCENT dbo.v_t_gescal_vigentes.r_object_id AS idOrigen, dbo.TD_LPARelaciones.codigoDocOrigen, 
                      dbo.v_t_gescal_vigentes.p_version AS versionDocOrigen, dbo.TD_LPARelaciones.codigoDocDestino, 
                      v_t_gescal_vigentes_1.p_version AS versionDocDestino, v_t_gescal_vigentes_1.r_object_id AS idDestino, dbo.TD_LPARelaciones.idrelacion, 
                      dbo.TD_LPARelaciones.usuario + ' (' + v_t_gescal_vigentes_1.estado + ' )' AS usuario, dbo.TD_LPARelaciones.fechaRelacion, 
                      v_t_gescal_vigentes_1.estado
FROM         dbo.TD_LPARelaciones INNER JOIN
                      dbo.v_t_gescal_vigentes ON dbo.TD_LPARelaciones.codigoDocOrigen = dbo.v_t_gescal_vigentes.p_codigo AND 
                      dbo.TD_LPARelaciones.versionDocOrigen = dbo.v_t_gescal_vigentes.p_version INNER JOIN
                      dbo.v_t_gescal_vigentes AS v_t_gescal_vigentes_1 ON dbo.TD_LPARelaciones.codigoDocDestino = v_t_gescal_vigentes_1.p_codigo AND 
                      dbo.TD_LPARelaciones.versionDocDestino = v_t_gescal_vigentes_1.p_version
ORDER BY dbo.TD_LPARelaciones.codigoDocDestino
GO
EXEC sys.sp_addextendedproperty @name=N'MS_DiagramPane1', @value=N'[0E232FF0-B466-11cf-A24F-00AA00A3EFFF, 1.00]
Begin DesignProperties = 
   Begin PaneConfigurations = 
      Begin PaneConfiguration = 0
         NumPanes = 4
         Configuration = "(H (1[39] 4[19] 2[19] 3) )"
      End
      Begin PaneConfiguration = 1
         NumPanes = 3
         Configuration = "(H (1[42] 4[17] 3) )"
      End
      Begin PaneConfiguration = 2
         NumPanes = 3
         Configuration = "(H (1[34] 2[8] 3) )"
      End
      Begin PaneConfiguration = 3
         NumPanes = 3
         Configuration = "(H (4[46] 2[24] 3) )"
      End
      Begin PaneConfiguration = 4
         NumPanes = 2
         Configuration = "(H (1[33] 3) )"
      End
      Begin PaneConfiguration = 5
         NumPanes = 2
         Configuration = "(H (2[24] 3) )"
      End
      Begin PaneConfiguration = 6
         NumPanes = 2
         Configuration = "(H (4 [50] 3))"
      End
      Begin PaneConfiguration = 7
         NumPanes = 1
         Configuration = "(V (3))"
      End
      Begin PaneConfiguration = 8
         NumPanes = 3
         Configuration = "(H (1[56] 4[18] 2) )"
      End
      Begin PaneConfiguration = 9
         NumPanes = 2
         Configuration = "(H (1 [75] 4))"
      End
      Begin PaneConfiguration = 10
         NumPanes = 2
         Configuration = "(H (1[66] 2) )"
      End
      Begin PaneConfiguration = 11
         NumPanes = 2
         Configuration = "(H (4 [60] 2))"
      End
      Begin PaneConfiguration = 12
         NumPanes = 1
         Configuration = "(H (1) )"
      End
      Begin PaneConfiguration = 13
         NumPanes = 1
         Configuration = "(V (4))"
      End
      Begin PaneConfiguration = 14
         NumPanes = 1
         Configuration = "(V (2))"
      End
      ActivePaneConfig = 1
   End
   Begin DiagramPane = 
      Begin Origin = 
         Top = 0
         Left = 0
      End
      Begin Tables = 
         Begin Table = "TD_LPARelaciones"
            Begin Extent = 
               Top = 6
               Left = 301
               Bottom = 182
               Right = 490
            End
            DisplayFlags = 280
            TopColumn = 1
         End
         Begin Table = "v_t_gescal_vigentes"
            Begin Extent = 
               Top = 4
               Left = 26
               Bottom = 201
               Right = 215
            End
            DisplayFlags = 280
            TopColumn = 0
         End
         Begin Table = "v_t_gescal_vigentes_1"
            Begin Extent = 
               Top = 26
               Left = 554
               Bottom = 174
               Right = 743
            End
            DisplayFlags = 280
            TopColumn = 0
         End
      End
   End
   Begin SQLPane = 
      PaneHidden = 
   End
   Begin DataPane = 
      Begin ParameterDefaults = ""
      End
      Begin ColumnWidths = 24
         Width = 284
         Width = 1560
         Width = 1470
         Width = 1470
         Width = 1575
         Width = 1530
         Width = 1560
         Width = 855
         Width = 2925
         Width = 900
         Width = 1755
         Width = 1500
         Width = 1500
         Width = 1500
         Width = 1500
         Width = 1500
         Width = 1500
         Width = 1500
         Width = 1500
         Width = 1500
         Width = 1500
         Width = 1500
         Width = 1500
         Width = 1500
      End
   End
   Begin CriteriaPane = 
      Begin ColumnWidths = 11
         Column = 2130
         Alias = 1860
         Table = 2370
         Output = 720
         Append = 1400
         NewValue = 1170
         SortType = 1350
         SortOr' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'VIEW',@level1name=N'v_relaciones_estados'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_DiagramPane2', @value=N'der = 1410
         GroupBy = 1350
         Filter = 1350
         Or = 1350
         Or = 1350
         Or = 1350
      End
   End
End
' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'VIEW',@level1name=N'v_relaciones_estados'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_DiagramPaneCount', @value=2 , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'VIEW',@level1name=N'v_relaciones_estados'
GO
/****** Object:  StoredProcedure [dbo].[DeleteRelaciones]    Script Date: 04/18/2012 09:56:01 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[DeleteAnexos]    Script Date: 04/18/2012 09:56:00 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[SelectAnexoPorID]    Script Date: 04/18/2012 09:56:06 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[SelectAnexos]    Script Date: 04/18/2012 09:56:07 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[InsertAnexo]    Script Date: 04/18/2012 09:56:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[SelectHistorialDocumento]    Script Date: 04/18/2012 09:56:08 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[SelectHistorialDocumento]
	@codigo varchar(64)='AT-AC-000044' ,
	@version varchar(8)='1.0',
	@r_object_id nchar(16) = '090003e98001c1f8'
AS
BEGIN

	--Obtiene el historial de un documento de Documentum
	--Base de datos : DM_documentum_docbase
	SELECT 
		a.r_object_id collate Latin1_General_CS_AI AS ID,
		a.chronicle_id collate Latin1_General_CS_AI as codigo,
		a.version_label collate Latin1_General_CS_AI AS version,
		a.event_name collate Latin1_General_CS_AI AS evento, 
		a.user_name collate Latin1_General_CS_AI AS usuario, 
		a.time_stamp AS fecha
	FROM 
		DM_documentum_docbase.dbo.dm_audittrail_s a,
		DM_documentum_docbase.dbo.dm_sysobject_s b

		--DM_documentum_docbase.dbo.dm_audittrail_s a INNER JOIN 
		--DM_documentum_docbase.dbo.dm_sysobject_s b ON b.r_object_id=@r_object_id 

	WHERE 
		b.r_object_id=@r_object_id AND
		a.chronicle_id = b.i_chronicle_id

	UNION
	 
	--Obtiene el historial de un documento 
	--Base de datos : TD_documentum
	SELECT 
		CONVERT(nchar(16),ID) AS ID,
		codigo=@codigo,
		version,
		CONVERT(nvarchar(64),evento) AS evento,
		CONVERT(nvarchar(64),usuario) AS usuario ,
		fecha 
	FROM
		TD_documentum.dbo.TD_LPAHistorial_Documento
	WHERE 
		codigo = @codigo 
		and version=@version

	ORDER BY fecha
END
GO
/****** Object:  StoredProcedure [dbo].[SelectHistorialDocumento1604]    Script Date: 04/18/2012 09:56:08 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- ======================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Obtiene Historial de un documento
-- ======================================================
CREATE PROCEDURE [dbo].[SelectHistorialDocumento1604] 
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
GO
/****** Object:  StoredProcedure [dbo].[InsertHistorialDocumento]    Script Date: 04/18/2012 09:56:03 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[DeleteHistorialDocVersion]    Script Date: 04/18/2012 09:56:00 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[SelectWorkflowBPM]    Script Date: 04/18/2012 09:56:12 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[SelectWorkflowBPM] 
	@workflow 	varchar(64)
AS
BEGIN

select packageAdjunto, packageBPM, claseBPM, propiedades from TP_LPAWorkflow_BPM where workflow = @workflow 

END
GO
/****** Object:  StoredProcedure [dbo].[InsertHistorialImpresion]    Script Date: 04/18/2012 09:56:03 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- ====================================================================
-- Author:		Juan Pulido
-- Create date: ---
-- Description: Insterta historial de impresiones
-- Update date:	16.04.2012
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
	@cantCopias int,
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
		idImpresora,
		CantCopias
	)
	values (
		@usuario, 
		getdate(),
		@tipoCopia, 
		@comentario, 
		@codigoDoc,
		@version, 
		@idDocumento,
		@idImpresora,
		@cantCopias
		
	)

	SET @ID = @@Identity
	PRINT @ID

END
GO
/****** Object:  StoredProcedure [dbo].[SelectWorkflowCommentsByDocId]    Script Date: 04/18/2012 09:56:12 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
GO
/****** Object:  StoredProcedure [dbo].[SelectRelaciones]    Script Date: 04/18/2012 09:56:10 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
				 ((codigoDocOrigen = @codigoDocOrigen and versionDocOrigen = @versionDocOrigen) 
			OR	 (codigoDocDestino = @codigoDocOrigen and versionDocDestino = @versionDocOrigen)
				  )  
			     

		END
END
GO
